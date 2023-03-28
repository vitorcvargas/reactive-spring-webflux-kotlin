package com.reactivespring.moviesservice.exception

class MoviesInfoClientException(message: String, val statusCode: Int) : RuntimeException(message)