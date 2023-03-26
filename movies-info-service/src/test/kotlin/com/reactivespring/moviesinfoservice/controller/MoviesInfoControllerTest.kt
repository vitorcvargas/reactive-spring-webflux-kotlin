package com.reactivespring.moviesinfoservice.controller

import com.ninjasquad.springmockk.MockkBean
import com.reactivespring.moviesinfoservice.domain.Actor
import com.reactivespring.moviesinfoservice.domain.MovieInfo
import com.reactivespring.moviesinfoservice.service.MoviesInfoService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@WebFluxTest(controllers = [MoviesInfoController::class])
@AutoConfigureWebTestClient
class MoviesInfoControllerTest {

    @MockkBean
    private lateinit var moviesInfoServiceMock: MoviesInfoService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private var MOVIES_INFO_URL = "/v1/movieinfos"

    @Test
    fun getAllMovieInfos() {
        val movieInfos = listOf(
            MovieInfo(
                null, "Batman Begins",
                2005, listOf(Actor("Christian Bale"), Actor("Michael Cane")), LocalDate.parse("2005-06-15")
            ),
            MovieInfo(
                null, "The Dark Knight",
                2008, listOf(Actor("Christian Bale"), Actor("HeathLedger")), LocalDate.parse("2008-07-18")
            ),
            MovieInfo(
                "abc", "Dark Knight Rises",
                2012, listOf(Actor("Christian Bale"), Actor("Tom Hardy")), LocalDate.parse("2012-07-20")
            )
        )

        every { moviesInfoServiceMock.getAllMovieInfos() } returns Flux.fromIterable(movieInfos)

        webTestClient
            .get()
            .uri(MOVIES_INFO_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBodyList(MovieInfo::class.java)
            .hasSize(3)
    }

    @Test
    fun getMovieInfoById() {
        val id = "abc"
        every { moviesInfoServiceMock.getMovieInfoById(any()) } returns
                Mono.just(
                    MovieInfo(
                        "abc", "Dark Knight Rises",
                        2012, listOf(Actor("Christian Bale"), Actor("Tom Hardy")), LocalDate.parse("2012-07-20")
                    )
                )

        webTestClient
            .get()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises")
    }

    @Test
    fun addNewMovieInfo() {
        val movieInfo = MovieInfo(
            null, "Batman Begins",
            2005, listOf(Actor("Christian Bale"), Actor("Michael Cane")), LocalDate.parse("2005-06-15")
        )
        every { moviesInfoServiceMock.addMovieInfo(any()) } returns
                Mono.just(
                    MovieInfo(
                        "mockId", "Batman Begins",
                        2005, listOf(Actor("Christian Bale"), Actor("Michael Cane")), LocalDate.parse("2005-06-15")
                    )
                )

        webTestClient
            .post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(MovieInfo::class.java)
            .consumeWith { movieInfoEntityExchangeResult ->
                val savedMovieInfo = movieInfoEntityExchangeResult.responseBody
                assert(savedMovieInfo!!.movieInfoId != null)
            }
    }

    @Test
    fun addNewMovieInfo_validation() {
        val movieInfo = MovieInfo(
            null, "",
            -2005, listOfNotNull(), LocalDate.parse("2005-06-15")
        )

        webTestClient
            .post()
            .uri(MOVIES_INFO_URL)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(String::class.java)
            .consumeWith { result ->
                val error = result.responseBody
                println("TEST: $error")
                val expectedErrorMessage =
                    "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a Positive Value"
                Assertions.assertEquals(expectedErrorMessage, error)
            }
    }


    @Test
    fun updateMovieInfo() {
        val id = "abc"
        val updatedMovieInfo = MovieInfo(
            "abc", "Dark Knight Rises 1",
            2013, listOf(Actor("Christian Bale1"), Actor("Tom Hardy1")), LocalDate.parse("2012-07-20")
        )

        every { moviesInfoServiceMock.updateMovieInfo(any(), any()) } returns
                Mono.just(updatedMovieInfo)

        webTestClient
            .put()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .bodyValue(updatedMovieInfo)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody(MovieInfo::class.java)
            .consumeWith { movieInfoEntityExchangeResult ->
                val movieInfo = movieInfoEntityExchangeResult.responseBody
                assertThat("Dark Knight Rises 1").isEqualTo(movieInfo!!.name)
            }
    }

    @Test
    fun updateMovieInfo_notFound() {
        val id = "abc1"
        val updatedMovieInfo = MovieInfo(
            "abc", "Dark Knight Rises 1",
            2013, listOf(Actor("Christian Bale1"), Actor("Tom Hardy1")), LocalDate.parse("2012-07-20")
        )
        every {
            moviesInfoServiceMock.updateMovieInfo(
                any(), any()
            )
        } returns Mono.empty()

        webTestClient
            .put()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .bodyValue(updatedMovieInfo)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun deleteMovieInfoById() {
        val id = "abc"

        every { moviesInfoServiceMock.deleteMovieInfoById(any()) } returns Mono.empty()

        webTestClient
            .delete()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .exchange()
            .expectStatus()
            .isNoContent
    }

}