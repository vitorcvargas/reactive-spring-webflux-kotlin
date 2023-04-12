package com.reactivespring.moviesservice.service

import com.reactivespring.moviesservice.client.ReviewsRestClient
import com.reactivespring.moviesservice.domain.Review
import com.reactivespring.moviesservice.exception.MoviesInfoClientException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.scheduler.Schedulers
import java.util.*

@Service
class ReviewService {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var reviewsRestClient: ReviewsRestClient

    fun createReviews(reviews: List<Review>): Flux<Review> {
        Hooks.onOperatorDebug();

        var validReviews = Collections.synchronizedSet(mutableSetOf<Boolean>())
        var reviewResponses = Collections.synchronizedList(mutableListOf<Review>())

        return Flux.fromIterable(reviews)
            .parallel(10)
            .runOn(Schedulers.parallel())
            .flatMap { review ->
                log.info("Object id is: ${review.reviewId}")

                reviewsRestClient.postReview(review, validReviews)
                    .doOnNext { review -> reviewResponses.add(review) }
            }
            .sequential()
            .takeLast(1)
            .flatMap {
                log.info("here")
                val reviewFlux = Flux.fromIterable(reviewResponses)

                if (validReviews.contains(false)) {
                    deleteReviews(reviewFlux)

                    Flux.error(
                        MoviesInfoClientException(
                            "It was not possible to post the reviews.",
                            400
                        )
                    )

                } else {
                    log.info("Finished reviews creation")

                    reviewFlux
                }
            }
    }

    private fun deleteReviews(reviewResponses: Flux<Review>) {
        reviewResponses
            .parallel(10)
            .runOn(Schedulers.parallel())
            .flatMap { review -> reviewsRestClient.deleteReview(review.reviewId!!) }
            .sequential()
            .takeLast(1)
            .subscribe()
    }
}