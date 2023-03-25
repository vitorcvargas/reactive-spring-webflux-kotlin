package com.reactivespring.moviesinfoservice.controller

import com.reactivespring.moviesinfoservice.domain.MovieInfo
import com.reactivespring.moviesinfoservice.service.MoviesInfoService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@RestController
@RequestMapping("/v1")
class MoviesInfoController(private val moviesInfoService: MoviesInfoService) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    var movieInfoSink = Sinks.many().replay().latest<MovieInfo>()

    @GetMapping("/movieinfos")
    fun getAllMovieInfos(@RequestParam(value = "year", required = false) year: Int?): Flux<MovieInfo> {
        log.info("year : {} ", year)
        return if (year != null) {
            moviesInfoService.getMovieInfoByYear(year).log()
        } else moviesInfoService.getAllMovieInfos()
    }

    @GetMapping(value = ["/movieinfos/stream"], produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun streamMovieInfos(): Flux<MovieInfo> {
        return movieInfoSink.asFlux()
    }

    @GetMapping("/movieinfos/{id}")
    fun getMovieInfoById_approach2(@PathVariable("id") id: String): Mono<ResponseEntity<MovieInfo>> {
        return moviesInfoService.getMovieInfoById(id)
            .map { movieInfo1 ->
                ResponseEntity.ok()
                    .body(movieInfo1)
            }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
            .log()
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    fun addMovieInfo(@RequestBody @Valid movieInfo: MovieInfo): Mono<MovieInfo> {
        return moviesInfoService.addMovieInfo(movieInfo)
            .doOnNext { savedMovieInfo -> movieInfoSink.tryEmitNext(savedMovieInfo) }
    }

    @PutMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateMovieInfo(
        @RequestBody movieInfo: MovieInfo,
        @PathVariable id: String
    ): Mono<ResponseEntity<MovieInfo>> {
        val updatedMovieInfoMono = moviesInfoService.updateMovieInfo(movieInfo, id)
        return updatedMovieInfoMono
            .map { updatedMovieInfo ->
                ResponseEntity.ok()
                    .body(updatedMovieInfo)
            }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMovieInfoById(@PathVariable id: String): Mono<Void> {
        return moviesInfoService.deleteMovieInfoById(id)
    }
}