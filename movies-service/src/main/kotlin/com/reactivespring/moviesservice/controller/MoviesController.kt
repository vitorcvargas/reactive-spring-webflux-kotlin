package com.reactivespring.moviesservice.controller

import com.reactivespring.moviesservice.client.MoviesInfoRestClient
import com.reactivespring.moviesservice.client.ReviewsRestClient
import com.reactivespring.moviesservice.domain.Movie
import com.reactivespring.moviesservice.domain.MovieInfo
import com.reactivespring.moviesservice.domain.Review
import com.reactivespring.moviesservice.service.ParallelService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/movies")
class MoviesController(
    var moviesInfoRestClient: MoviesInfoRestClient,
    var reviewsRestClient: ReviewsRestClient,
    var reviewService: ParallelService
) {

    @GetMapping("/{id}")
    fun retrieveMovieById(@PathVariable("id") movieId: String): Mono<Movie> {

        return moviesInfoRestClient.retrieveMovieInfo(movieId)
            .flatMap { movieInfo ->
                val reviewList = reviewsRestClient.retrieveReviews(movieId)
                    .collectList()

                reviewList.map { reviews: List<Review> -> Movie(movieInfo, reviews) }
            }
    }

    @GetMapping(value = ["/stream"], produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun retrieveMovieInfos(): Flux<MovieInfo> {
        return moviesInfoRestClient.retrieveMovieInfoStream()
    }

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun createReviews(@RequestBody reviews: List<Review>): Flux<Review> {
        return reviewService.createReviews(reviews)
    }

}