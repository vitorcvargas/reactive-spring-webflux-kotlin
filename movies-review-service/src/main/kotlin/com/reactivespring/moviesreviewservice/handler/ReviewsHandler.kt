package com.reactivespring.moviesreviewservice.handler

import com.reactivespring.moviesreviewservice.domain.Review
import com.reactivespring.moviesreviewservice.exception.ReviewDataException
import com.reactivespring.moviesreviewservice.repository.ReviewReactiveRepository
import com.reactivespring.moviesreviewservice.validator.ReviewValidator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Errors
import org.springframework.validation.ObjectError
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.lang.String.format
import java.util.stream.Collectors

@Component
class ReviewsHandler(var reviewReactiveRepository: ReviewReactiveRepository, var reviewValidator: ReviewValidator) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    var reviewsSink = Sinks.many().replay().latest<Review>()

    fun getReviews(serverRequest: ServerRequest): Mono<ServerResponse> {
        val movieInfoId = serverRequest.queryParam("movieInfoId")
        return if (movieInfoId.isPresent) {
            val reviews =
                reviewReactiveRepository.findReviewsByMovieInfoId(movieInfoId.get().toLong())
            buildReviewsResponse(reviews)
        } else {
            val reviews = reviewReactiveRepository.findAll()
            buildReviewsResponse(reviews)
        }
    }

    private fun buildReviewsResponse(reviews: Flux<Review>): Mono<ServerResponse> {
        return ServerResponse.ok()
            .body(reviews, Review::class.java)
    }

    fun addReview(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono(Review::class.java)
            .doOnNext { review: Review -> validate(review) }
            .flatMap { review -> reviewReactiveRepository.save(review) }
            .doOnNext { review -> reviewsSink.tryEmitNext(review) }
            .flatMap { savedReview ->
                ServerResponse.status(HttpStatus.CREATED)
                    .bodyValue(savedReview)
            }
    }

    private fun validate(review: Review) {
        val errors: Errors = BeanPropertyBindingResult(review, "review")

        reviewValidator.validate(review, errors)

        if (errors.hasErrors()) {
            val errorMessage = errors.allErrors
                .stream()
                .map { error: ObjectError -> format("%s:%s", error.code, error.defaultMessage) }
                .sorted()
                .collect(Collectors.joining(", "))
            log.info("errorMessage : {} ", errorMessage)
            throw ReviewDataException(errorMessage)
        }
    }

    fun updateReview(serverRequest: ServerRequest): Mono<ServerResponse> {
        val reviewId = serverRequest.pathVariable("id")
        val existingReview = reviewReactiveRepository.findById(reviewId)

        return existingReview
            .flatMap { review ->
                serverRequest.bodyToMono(Review::class.java)
                    .map { reqReview ->
                        review.comment = reqReview.comment
                        review.rating = reqReview.rating
                        review
                    }
                    .flatMap(reviewReactiveRepository::save)
                    .flatMap { savedReview ->
                        ServerResponse.status(HttpStatus.OK)
                            .bodyValue(savedReview)
                    }
            }
            .switchIfEmpty(notFound)
    }

    fun deleteReview(serverRequest: ServerRequest): Mono<ServerResponse> {
        val reviewId = serverRequest.pathVariable("id")

        return reviewReactiveRepository.findById(reviewId)
            .flatMap {
                reviewReactiveRepository.deleteById(reviewId)
            }
            .then(ServerResponse.noContent().build())
    }

    fun getReviewsStream(serverRequest: ServerRequest?): Mono<ServerResponse> {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_NDJSON)
            .body(reviewsSink.asFlux(), Review::class.java)
            .log()
    }

    companion object {
        var notFound = ServerResponse.notFound().build()
    }
}
