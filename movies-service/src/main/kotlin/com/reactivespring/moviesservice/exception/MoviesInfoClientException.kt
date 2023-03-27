package com.reactivespring.moviesservice.exception

class MoviesInfoClientException(override var message: String, var statusCode: Int) : RuntimeException(message)