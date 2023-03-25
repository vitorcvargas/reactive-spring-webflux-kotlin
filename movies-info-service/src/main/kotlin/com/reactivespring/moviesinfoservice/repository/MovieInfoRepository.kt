package com.reactivespring.moviesinfoservice.repository

import com.reactivespring.moviesinfoservice.domain.MovieInfo
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MovieInfoRepository : ReactiveMongoRepository<MovieInfo, String> {
    fun findByYear(year: Int): Flux<MovieInfo>
    fun findByName(name: String): Mono<MovieInfo>
}