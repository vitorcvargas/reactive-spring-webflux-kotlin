package com.reactivespring.moviesservice.exception

class MoviesInfoServerException(override val message: String) : RuntimeException(message)