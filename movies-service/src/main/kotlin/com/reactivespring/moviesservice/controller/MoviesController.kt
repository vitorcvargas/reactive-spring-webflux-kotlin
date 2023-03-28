package com.reactivespring.moviesservice.controller

import com.reactivespring.moviesservice.client.MoviesInfoRestClient
import com.reactivespring.moviesservice.client.ReviewsRestClient
import com.reactivespring.moviesservice.domain.Movie
import com.reactivespring.moviesservice.domain.MovieInfo
import com.reactivespring.moviesservice.domain.Review
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/v1/movies")
class MoviesController(var moviesInfoRestClient: MoviesInfoRestClient, var reviewsRestClient: ReviewsRestClient) {

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
}