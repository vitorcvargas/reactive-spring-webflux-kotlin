package com.reactivespring.moviesservice.domain

data class Review(
    var reviewId: String?,
    var movieInfoId: Long,
    var comment: String,
    val rating: Double? = null
)