package com.reactivespring.moviesservice.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.reactivespring.moviesservice.domain.Movie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.WireMockSpring
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoviesControllerIntgTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    var wiremock = WireMockServer(WireMockSpring.options().port(8084))

    @BeforeAll
    fun setupClass() {
        wiremock.start()
    }

    @AfterEach
    fun after() {
        wiremock.resetAll()
    }

    @AfterAll
    fun clean() {
        wiremock.shutdown()
    }

    @Test
    fun retrieveMovieById() {

        val movieId = "abc"

        wiremock.stubFor(
            get(urlEqualTo("/v1/movieinfos/$movieId"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                )
        )

        wiremock.stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")
                )
        )

        webTestClient.get()
            .uri("/v1/movies/{id}", "abc")
            .exchange()
            .expectStatus().isOk
            .expectBody(Movie::class.java)
            .consumeWith { movieEntityExchangeResult ->
                val movie = movieEntityExchangeResult.responseBody
                assertThat(movie!!.reviewList!!.size).isEqualTo(2)
                assertThat(movie.movieInfo!!.name).isEqualTo("Batman Begins")
            }
    }

    @Test
    fun retrieveMovieById_404() {

        val movieId = "abc"

        wiremock.stubFor(
            get(urlEqualTo("/v1/movieinfos/$movieId"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                )
        )

        wiremock.stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                )
        )

        webTestClient.get()
            .uri("/v1/movies/{id}", "abc")
            .exchange()
            .expectStatus().is4xxClientError
    }

    @Test
    fun retrieveMovieById_Reviews_404() {

        val movieId = "abc"

        wiremock.stubFor(
            get(urlEqualTo("/v1/movieinfos/$movieId"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                )
        )

        wiremock.stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                )
        )

        webTestClient.get()
            .uri("/v1/movies/{id}", "abc")
            .exchange()
            .expectStatus().is2xxSuccessful
    }

    @Test
    fun retrieveMovieById_5XX() {

        val movieId = "abc"

        wiremock.stubFor(
            get(urlEqualTo("/v1/movieinfos/$movieId"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo Service Unavailable")
                )
        )

        webTestClient.get()
            .uri("/v1/movies/{id}", "abc")
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody(String::class.java)
            .value { message: String? ->
                Assertions.assertEquals(
                    "MovieInfo Service Unavailable",
                    message
                )
            }

        wiremock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos/$movieId")))
    }

    @Test
    fun retrieveMovieById_reviews_5XX() {

        val movieId = "abc"

        wiremock.stubFor(
            get(urlEqualTo("/v1/movieinfos/$movieId"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")
                )
        )

        wiremock.stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .withQueryParam("movieInfoId", equalTo(movieId))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withBody("Review Service Unavailable")
                )
        )

        webTestClient.get()
            .uri("/v1/movies/{id}", "abc")
            .exchange()
            .expectStatus().is5xxServerError
            .expectBody(String::class.java)
            .value { message ->
                Assertions.assertEquals(
                    "Review Service Unavailable",
                    message
                )
            }

        wiremock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")))
    }
}