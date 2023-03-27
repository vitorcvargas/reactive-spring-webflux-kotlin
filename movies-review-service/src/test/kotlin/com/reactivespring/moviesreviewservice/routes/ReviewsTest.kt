package com.reactivespring.moviesreviewservice.routes

import com.ninjasquad.springmockk.MockkBean
import com.reactivespring.moviesreviewservice.domain.Review
import com.reactivespring.moviesreviewservice.exceptionhandler.GlobalErrorHandler
import com.reactivespring.moviesreviewservice.handler.ReviewsHandler
import com.reactivespring.moviesreviewservice.repository.ReviewReactiveRepository
import com.reactivespring.moviesreviewservice.validator.ReviewValidator
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@WebFluxTest
@ContextConfiguration(classes = [ReviewRouter::class, ReviewsHandler::class, GlobalErrorHandler::class])
@AutoConfigureWebTestClient
class ReviewsTest {

    @MockkBean
    lateinit var reviewReactiveRepository: ReviewReactiveRepository

    @MockkBean
    lateinit var reviewValidator: ReviewValidator

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun getAllReviews() {
        //given
        val reviewList = listOf(
            Review(null, 1L, "Awesome Movie", 9.0),
            Review(null, 1L, "Awesome Movie1", 9.0),
            Review(null, 2L, "Excellent Movie", 8.0)
        )
        every { reviewReactiveRepository.findAll() } returns Flux.fromIterable(reviewList)

        //when
        webTestClient
            .get()
            .uri("/v1/reviews")
            .exchange()
            .expectStatus()
            .is2xxSuccessful
            .expectBodyList(Review::class.java)
            .value<ListBodySpec<Review>> { reviews: List<Review> -> assertThat(reviews.size).isEqualTo(3) }
    }

    @Test
    fun addReview() {
        //given
        val review = Review(null, 1L, "Awesome Movie", 9.0)

        every { reviewReactiveRepository.save(any()) } returns
                Mono.just(Review("abc", 1L, "Awesome Movie", 9.0))

        every { reviewValidator.validate(any(), any()) } answers { callOriginal() }

        //when
        webTestClient
            .post()
            .uri("/v1/reviews")
            .bodyValue(review)
            .exchange()
            .expectStatus().isCreated
            .expectBody(Review::class.java)
            .consumeWith { reviewResponse ->
                val savedReview = reviewResponse.responseBody!!
                Assertions.assertNotNull(savedReview.reviewId)
                assertThat(savedReview.reviewId).isEqualTo("abc")
            }
    }

    @Test
    fun addReview_Validations() {
        //given
        val review = Review(null, null, "Awesome Movie", -9.0)

        every { reviewValidator.validate(any(), any()) } answers { callOriginal() }

        //when
        webTestClient
            .post()
            .uri("/v1/reviews")
            .bodyValue(review)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(String::class.java)
            .isEqualTo("movieInfoId.null : Pass a valid movieInfoId, rating.negative : rating is negative and please pass a non-negative value")
    }

    @Test
    fun updateReview() {
        //given
        val reviewUpdate = Review(null, 1L, "Not an Awesome Movie", 8.0)

        every { reviewReactiveRepository.save(any()) } returns Mono.just(Review("abc", 1L, "Not an Awesome Movie", 8.0))
        every { reviewReactiveRepository.findById(any() as String) } returns Mono.just(
            Review(
                "abc",
                1L,
                "Awesome Movie",
                9.0
            )
        )

        //when
        webTestClient
            .put()
            .uri("/v1/reviews/{id}", "abc")
            .bodyValue(reviewUpdate)
            .exchange()
            .expectStatus().isOk
            .expectBody(Review::class.java)
            .consumeWith { reviewResponse ->
                val updatedReview = reviewResponse.responseBody!!
                println("updatedReview : $updatedReview")
                assertThat(updatedReview.rating).isEqualTo(8.0)
                assertThat(updatedReview.comment).isEqualTo("Not an Awesome Movie")
            }
    }

    @Test
    fun deleteReview() {
        //given
        val reviewId = "abc"

        every { reviewReactiveRepository.findById(any() as String) } returns
                Mono.just(Review("abc", 1L, "Awesome Movie", 9.0))

        every { reviewReactiveRepository.deleteById(any() as String) } returns Mono.empty()

        //when
        webTestClient
            .delete()
            .uri("/v1/reviews/{id}", reviewId)
            .exchange()
            .expectStatus().isNoContent
    }
}