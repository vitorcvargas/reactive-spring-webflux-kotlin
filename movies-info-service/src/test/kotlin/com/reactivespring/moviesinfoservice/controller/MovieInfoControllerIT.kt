package com.reactivespring.moviesinfoservice.controller

import com.reactivespring.moviesinfoservice.domain.Actor
import com.reactivespring.moviesinfoservice.domain.MovieInfo
import com.reactivespring.moviesinfoservice.repository.MovieInfoRepository
import com.reactivespring.moviesinfoservice.utils.containers.MongoContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.test.StepVerifier
import java.time.LocalDate
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext
class MovieInfoControllerIT {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var movieInfoRepository: MovieInfoRepository

    private var MOVIES_INFO_URL = "/v1/movieinfos"

    private val MONGO_CONTAINER = MongoContainer.getInstance()

    init {
        MONGO_CONTAINER.start()
        System.setProperty("spring.data.mongodb.uri", MONGO_CONTAINER.replicaSetUrl);
    }

    @BeforeEach
    fun setUp() {
        val movieinfos = listOf(
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

        movieInfoRepository
            .deleteAll()
            .thenMany(movieInfoRepository.saveAll(movieinfos))
            .blockLast()
    }

    @Test
    fun getAllMovieInfos() {
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
    fun getAllMovieInfos_Stream() {
        val movieInfo = MovieInfo(
            null, "Batman Begins",
            2005, listOf(Actor("Christian Bale"), Actor("Michael Cane")), LocalDate.parse("2005-06-15")
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
                assertThat(savedMovieInfo!!.movieInfoId).isNotNull
            }

        val moviesStreamFlux = webTestClient
            .get()
            .uri("$MOVIES_INFO_URL/stream")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .returnResult(MovieInfo::class.java)
            .responseBody

        StepVerifier.create(moviesStreamFlux)
            .assertNext { movieInfo1 -> assertThat(movieInfo1.movieInfoId).isNotNull }
            .thenCancel()
            .verify()
    }

    @Test
    fun getMovieInfoByYear() {
        val uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
            .queryParam("year", 2005)
            .buildAndExpand().toUri()

        webTestClient
            .get()
            .uri(uri)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBodyList(MovieInfo::class.java)
            .hasSize(1)
    }

    @Test
    fun addNewMovieInfo() {
        val movieInfo = MovieInfo(
            null, "Batman Begins",
            2005, listOf(Actor("Christian Bale"), Actor("Michael Cane")), LocalDate.parse("2005-06-15")
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
                assertThat(savedMovieInfo!!.movieInfoId).isNotNull
            }
    }

    @Test
    fun getMovieInfoById() {
        val id = "abc"

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
    fun getMovieInfoById_1() {
        val id = "def"

        webTestClient
            .get()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun updateMovieInfo() {
        val id = "abc"
        val updatedMovieInfo = MovieInfo(
            "abc", "Dark Knight Rises 1",
            2013, listOf(Actor("Christian Bale1"), Actor("Tom Hardy1")), LocalDate.parse("2012-07-20")
        )

        webTestClient
            .put()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .bodyValue(updatedMovieInfo)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBody(MovieInfo::class.java)
            .consumeWith { movieInfoEntityExchangeResult ->
                val movieInfo = movieInfoEntityExchangeResult.responseBody!!
                assertThat(movieInfo.name).isEqualTo("Dark Knight Rises 1")
            }
    }

    @Test
    fun updateMovieInfo_notFound() {
        val id = "abc1"
        val updatedMovieInfo = MovieInfo(
            "abc", "Dark Knight Rises 1",
            2013, listOf(Actor("Christian Bale1"), Actor("Tom Hardy1")), LocalDate.parse("2012-07-20")
        )

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
        webTestClient
            .delete()
            .uri("$MOVIES_INFO_URL/{id}", id)
            .exchange()
            .expectStatus()
            .isNoContent
    }
}