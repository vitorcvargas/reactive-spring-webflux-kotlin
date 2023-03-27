package com.reactivespring.moviesreviewservice.exceptionhandler

import com.reactivespring.moviesreviewservice.exception.ReviewDataException
import com.reactivespring.moviesreviewservice.exception.ReviewNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class GlobalErrorHandler : ErrorWebExceptionHandler {

    private val log = LoggerFactory.getLogger(this.javaClass)
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        log.error("Exception Message is : {} ", ex.message, ex)
        val bufferFactory = exchange.response.bufferFactory()
        val errorMessage = bufferFactory.wrap(ex.message!!.toByteArray())
        if (ex is ReviewNotFoundException) {
            exchange.response.statusCode = HttpStatus.NOT_FOUND
            return exchange.response.writeWith(Mono.just(errorMessage))
        }
        if (ex is ReviewDataException) {
            exchange.response.statusCode = HttpStatus.BAD_REQUEST
            return exchange.response.writeWith(Mono.just(errorMessage))
        }
        exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
        return exchange.response.writeWith(Mono.just(errorMessage))
    }
}