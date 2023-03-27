package com.reactivespring.moviesservice.client

import com.reactivespring.moviesservice.domain.MovieInfo
import com.reactivespring.moviesservice.exception.MoviesInfoClientException
import com.reactivespring.moviesservice.exception.MoviesInfoServerException
import com.reactivespring.moviesservice.util.RetryUtil
import io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND
import io.netty.handler.codec.http.HttpResponseStatus.OK
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class MoviesInfoRestClient(private val webClient: WebClient) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Value("\${restClient.moviesInfoUrl}")
    lateinit var moviesInfoUrl: String
    fun retrieveMovieInfo(movieId: String): Mono<MovieInfo>? {
        val url = "$moviesInfoUrl/{id}"

        return webClient.get()
            .uri(url, movieId)
            .retrieve()
            .onStatus({ obj: HttpStatusCode -> obj.is4xxClientError }) { clientResponse ->

                log.info("Status code : {}", clientResponse.statusCode().value())

                if ((clientResponse.statusCode() == HttpStatus.NOT_FOUND)) {
                    Mono.error<Throwable>(
                        MoviesInfoClientException(
                            "There is no MovieInfo available for the passed in Id : $movieId",
                            clientResponse.statusCode().value()
                        )
                    )
                }

                clientResponse.bodyToMono(String::class.java)
                    .flatMap { response: String ->
                        Mono.error(
                            MoviesInfoClientException(response, clientResponse.statusCode().value())
                        )
                    }
            }
            .onStatus({ obj: HttpStatusCode -> obj.is4xxClientError }) { clientResponse ->

                log.info("Status code : {}", clientResponse.statusCode().value())

                clientResponse.bodyToMono(String::class.java)
                    .flatMap { response: String ->
                        Mono.error(
                            MoviesInfoServerException(response)
                        )
                    }
            }
            .bodyToMono(MovieInfo::class.java)
            .retryWhen(RetryUtil.retrySpec())
            .log()
    }

    fun retrieveMovieInfoStream(): Flux<MovieInfo> {
        val url = "$moviesInfoUrl/stream"

        return webClient.get()
            .uri(url)
            .retrieve()
            .onStatus({ obj: HttpStatusCode -> obj.is4xxClientError }) { clientResponse ->

                log.info("Status code : {}", clientResponse.statusCode().value())

                clientResponse.bodyToMono(String::class.java)
                    .flatMap { response: String ->
                        Mono.error(
                            MoviesInfoClientException(response, clientResponse.statusCode().value())
                        )
                    }
            }
            .onStatus({ obj: HttpStatusCode -> obj.is5xxServerError }) { clientResponse ->

                log.info("Status code : {}", clientResponse.statusCode().value())

                clientResponse.bodyToMono(String::class.java)
                    .flatMap { response: String ->
                        Mono.error(
                            MoviesInfoServerException(response)
                        )
                    }
            }
            .bodyToFlux(MovieInfo::class.java)
            .retryWhen(RetryUtil.retrySpec())
            .log()
    }

    fun retrieveMovieInfo_exchange(movieId: String): Mono<MovieInfo> {
        val url = "$moviesInfoUrl/{id}"

        return webClient.get()
            .uri(url, movieId)
            .exchangeToMono { clientResponse: ClientResponse ->
                when (clientResponse.statusCode()) {
                    OK -> clientResponse.bodyToMono(
                        MovieInfo::class.java
                    )

                    NOT_FOUND -> Mono.error(
                        MoviesInfoClientException(
                            "There is no MovieInfo available for the passed in Id : $movieId",
                            clientResponse.statusCode().value()
                        )
                    )

                    else -> clientResponse.bodyToMono(String::class.java)
                        .flatMap { response ->
                            Mono.error(
                                MoviesInfoServerException(
                                    response!!
                                )
                            )
                        }
                }
            }
            .retryWhen(RetryUtil.retrySpec())
            .log()
    }
}
