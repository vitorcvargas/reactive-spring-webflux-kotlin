package com.reactivespring.moviesservice.client

import com.reactivespring.moviesservice.domain.Review
import com.reactivespring.moviesservice.exception.ReviewsClientException
import com.reactivespring.moviesservice.exception.ReviewsServerException
import com.reactivespring.moviesservice.util.RetryUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ReviewsRestClient(private val webClient: WebClient) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Value("\${restClient.reviewsUrl}")
    lateinit var reviewsUrl: String

    fun retrieveReviews(movieId: String): Flux<Review> {
        val url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
            .queryParam("movieInfoId", movieId)
            .buildAndExpand().toString()

        return webClient.get()
            .uri(url)
            .retrieve()
            .onStatus({ obj: HttpStatusCode -> obj.is4xxClientError }) { clientResponse ->
                log.info("Status code : {}", clientResponse.statusCode().value())

                if ((clientResponse.statusCode() == HttpStatus.NOT_FOUND)) {
                    Mono.empty<Throwable>()
                }

                clientResponse.bodyToMono(String::class.java)
                    .flatMap { response ->
                        Mono.error(
                            ReviewsClientException(response)
                        )
                    }
            }
            .onStatus({ obj: HttpStatusCode -> obj.is5xxServerError }) { clientResponse ->

                log.info("Status code : {}", clientResponse.statusCode().value())

                clientResponse.bodyToMono(String::class.java)
                    .flatMap { response ->
                        Mono.error(
                            ReviewsServerException(response)
                        )
                    }
            }
            .bodyToFlux(Review::class.java)
            .retryWhen(RetryUtil.retrySpec())
    }
}