package com.reactivespring.moviesreviewservice.routes

import com.reactivespring.moviesreviewservice.handler.ReviewsHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.*

@Configuration
class ReviewRouter {
    @Bean
    fun reviewsRoute(reviewsHandler: ReviewsHandler): RouterFunction<ServerResponse> {
        return RouterFunctions.route()
            .nest(
                RequestPredicates.path("/v1/reviews")
            ) { builder: RouterFunctions.Builder ->
                builder
                    .GET("", reviewsHandler::getReviews)
                    .POST("", reviewsHandler::addReview)
                    .PUT("/{id}", reviewsHandler::updateReview)
                    .DELETE("/{id}", reviewsHandler::deleteReview)
                    .GET("/stream", reviewsHandler::getReviewsStream)
            }
            .GET("/v1/helloworld") {
                ServerResponse.ok().bodyValue("HelloWorld")
            }
            .GET("/v1/greeting/{name}") { request: ServerRequest ->
                ServerResponse.ok().bodyValue("hello " + request.pathVariable("name"))
            }
            .build()
    }
}