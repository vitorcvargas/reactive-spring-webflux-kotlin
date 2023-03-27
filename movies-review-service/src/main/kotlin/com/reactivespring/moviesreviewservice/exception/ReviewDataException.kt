package com.reactivespring.moviesreviewservice.exception

class ReviewDataException(override var message: String) : RuntimeException(message)