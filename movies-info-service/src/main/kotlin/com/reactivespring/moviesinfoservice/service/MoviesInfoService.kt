package com.reactivespring.moviesinfoservice.service

import com.reactivespring.moviesinfoservice.domain.MovieInfo
import com.reactivespring.moviesinfoservice.repository.MovieInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MoviesInfoService(private val movieInfoRepository: MovieInfoRepository) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun getAllMovieInfos(): Flux<MovieInfo> {
        return movieInfoRepository.findAll()
    }

    fun getMovieInfoByYear(year: Int): Flux<MovieInfo> {
        return movieInfoRepository.findByYear(year)
    }

    fun addMovieInfo(movieInfo: MovieInfo): Mono<MovieInfo> {
        log.info("addMovieInfo : {} ", movieInfo)
        return movieInfoRepository.save(movieInfo)
            .log()
    }

    fun getMovieInfoById(id: String): Mono<MovieInfo> {
        return movieInfoRepository.findById(id)
    }

    fun updateMovieInfo(movieInfo: MovieInfo, id: String): Mono<MovieInfo> {
        return movieInfoRepository.findById(id)
            .flatMap { movieInfo1 ->
                movieInfo1.cast = movieInfo.cast
                movieInfo1.name = movieInfo.name
                movieInfo1.release_date = movieInfo.release_date
                movieInfo1.year = movieInfo.year
                movieInfoRepository.save(movieInfo1)
            }
    }

    fun deleteMovieInfoById(id: String): Mono<Void> {
        return movieInfoRepository.deleteById(id)
    }
}