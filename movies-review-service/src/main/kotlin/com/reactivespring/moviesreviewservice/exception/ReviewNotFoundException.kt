package com.reactivespring.moviesreviewservice.exception

class ReviewNotFoundException : RuntimeException {
    override var message: String
    var ex: Throwable? = null

    constructor(message: String, ex: Throwable) : super(message, ex) {
        this.message = message
        this.ex = ex
    }

    constructor(message: String) : super(message) {
        this.message = message
    }
}