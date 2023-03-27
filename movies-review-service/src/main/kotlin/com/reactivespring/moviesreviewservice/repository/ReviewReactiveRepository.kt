package com.reactivespring.moviesreviewservice.repository

import com.reactivespring.moviesreviewservice.domain.Review
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface ReviewReactiveRepository : ReactiveMongoRepository<Review, String> {
    fun findReviewsByMovieInfoId(movieInfoId: Long): Flux<Review>
}
