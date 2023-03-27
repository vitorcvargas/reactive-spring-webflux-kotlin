package com.reactivespring.moviesservice.domain

data class Movie(
    var movieInfo: MovieInfo?,
    var reviewList: List<Review>?
)