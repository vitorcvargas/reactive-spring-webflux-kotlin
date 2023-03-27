package com.reactivespring.moviesreviewservice.domain

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Review(
    @Id
    var reviewId: String?,

    @get:NotNull(message = "rating.movieInfoId : must not be null")
    var movieInfoId: Long?,

    var comment: String? = null,

    @get:Min(value = 0L, message = "rating.negative : please pass a non-negative value")
    var rating: Double
)