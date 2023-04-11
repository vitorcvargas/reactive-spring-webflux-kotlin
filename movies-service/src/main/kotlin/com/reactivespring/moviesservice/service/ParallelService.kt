package com.reactivespring.moviesservice.service

import com.reactivespring.moviesservice.client.ReviewsRestClient
import com.reactivespring.moviesservice.domain.Review
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers


@Service
class ParallelService {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var reviewsRestClient: ReviewsRestClient

    fun createReviews(reviews: List<Review>): Flux<Review> {
        log.info("Should be logged once, when method called.")

        var validReviews = HashSet<Boolean>()
        var reviewResponses = ArrayList<Review>()

        return Flux.fromIterable(reviews)
            .parallel(10)
            .runOn(Schedulers.parallel())
            .flatMap { review ->
                log.info("Object id is: ${review.reviewId}")

                reviewsRestClient.postReview(review, validReviews, reviewResponses)
                    .doOnNext { review -> reviewResponses.add(review) }
            }
            .sequential()
            .doOnComplete {
                log.info("Should be logged once, only after ParallelFlux completion.")

                if (validReviews.contains(false)) {
                    log.info("Send Message to handle deletion.")

                    Flux.fromIterable(reviewResponses)
                        .flatMap { review -> reviewsRestClient.deleteReview(review.reviewId!!) }
                        .subscribe()

                    println(reviewResponses)

                    Flux.empty()
                } else {
                    Flux.fromIterable(reviewResponses)
                }
            }
    }
}