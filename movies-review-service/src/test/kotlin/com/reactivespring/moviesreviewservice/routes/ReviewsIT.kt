package com.reactivespring.moviesreviewservice.routes

import com.reactivespring.moviesreviewservice.domain.Review
import com.reactivespring.moviesreviewservice.repository.ReviewReactiveRepository
import com.reactivespring.moviesreviewservice.utils.containers.MongoContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import org.springframework.web.util.UriBuilder
import reactor.test.StepVerifier
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext
class ReviewIT {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var reviewReactiveRepository: ReviewReactiveRepository

    var REVIEWS_URL = "/v1/reviews"

    private val MONGO_CONTAINER = MongoContainer.getInstance()

    init {
        MONGO_CONTAINER.start()
        System.setProperty("spring.data.mongodb.uri", MONGO_CONTAINER.replicaSetUrl);
    }

    @BeforeEach
    fun setUp() {

        val reviewsList = listOf(
            Review(null, 1L, "Awesome Movie", 9.0),
            Review(null, 1L, "Awesome Movie1", 9.0),
            Review(null, 2L, "Excellent Movie", 8.0)
        )
        reviewReactiveRepository.saveAll(reviewsList)
            .blockLast()
    }

    @AfterEach
    fun tearDown() {
        reviewReactiveRepository.deleteAll()
            .block()
    }

    @Test
    fun name() {

        webTestClient
            .get()
            .uri("/v1/helloworld")
            .exchange()
            .expectBody(String::class.java)
            .isEqualTo("HelloWorld")
    }

    @Test
    fun getReviews() {

        webTestClient
            .get()
            .uri(REVIEWS_URL)
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBodyList(Review::class.java)
            .value<ListBodySpec<Review>> { reviews ->
                assertThat(reviews.size).isEqualTo(3)
            }
    }

    @Test
    fun getReviews_Stream() {
        val review = Review(null, 1L, "Awesome Movie", 9.0)

        webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(review)
            .exchange()
            .expectStatus()
            .isCreated
            .expectBody(Review::class.java)
            .consumeWith { movieInfoEntityExchangeResult ->
                val savedReview = movieInfoEntityExchangeResult.responseBody
                assertThat(savedReview!!.reviewId).isNotNull
            }

        val reviewStreamFlux = webTestClient
            .get()
            .uri("$REVIEWS_URL/stream")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .returnResult(Review::class.java)
            .responseBody

        StepVerifier.create(reviewStreamFlux)
            .assertNext { rev -> assertThat(rev.reviewId).isNotNull }
            .thenCancel()
            .verify()
    }


    @Test
    fun getReviewsByMovieInfoId() {

        webTestClient
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder.path(REVIEWS_URL)
                    .queryParam("movieInfoId", "1")
                    .build()
            }
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBodyList(Review::class.java)
            .value<ListBodySpec<Review>> { reviewList ->
                println("reviewList : $reviewList")
                assertThat(reviewList.size).isEqualTo(2)
            }
    }

    @Test
    fun addReview() {
        val review = Review(null, 1L, "Awesome Movie", 9.0)

        webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(review)
            .exchange()
            .expectStatus().isCreated
            .expectBody(Review::class.java)
            .consumeWith { reviewResponse ->
                val savedReview = reviewResponse.responseBody!!
                assertThat(savedReview.reviewId).isNotNull
            }
    }

    @Test
    fun updateReview() {
        val review = Review(null, 1L, "Awesome Movie", 9.0)
        val savedReview = reviewReactiveRepository.save(review).block()
        val reviewUpdate = Review(null, 1L, "Not an Awesome Movie", 8.0)

        assertThat(savedReview).isNotNull

        webTestClient
            .put()
            .uri("$REVIEWS_URL/{id}", savedReview!!.reviewId)
            .bodyValue(reviewUpdate)
            .exchange()
            .expectStatus().isOk
            .expectBody(Review::class.java)
            .consumeWith { reviewResponse ->
                val updatedReview = reviewResponse.responseBody!!
                println("updatedReview : $updatedReview")
                assertThat(savedReview).isNotNull
                assertThat(updatedReview.rating).isEqualTo(8.0)
                assertThat(updatedReview.comment).isEqualTo("Not an Awesome Movie")
            }
    }

    @Test
    fun updateReview_NotFound() {
        val reviewUpdate = Review(null, 1L, "Not an Awesome Movie", 8.0)

        webTestClient
            .put()
            .uri("$REVIEWS_URL/{id}", "abc")
            .bodyValue(reviewUpdate)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun deleteReview() {
        val review = Review(null, 1L, "Awesome Movie", 9.0)
        val savedReview = reviewReactiveRepository.save(review).block()!!

        webTestClient
            .delete()
            .uri("$REVIEWS_URL/{id}", savedReview.reviewId)
            .exchange()
            .expectStatus().isNoContent
    }
}