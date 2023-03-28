package com.reactivespring.moviesservice.exceptionhandler

import com.reactivespring.moviesservice.exception.MoviesInfoClientException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalErrorHandler {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(MoviesInfoClientException::class)
    fun handleClientException(ex: MoviesInfoClientException): ResponseEntity<String> {
        log.error("Exception caught in handleClientException :  {} ", ex.message, ex)
        log.info("Status value is : {}", ex.statusCode)
        return ResponseEntity(ex.message, HttpStatus.valueOf(ex.statusCode))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<String> {
        log.error("Exception caught in handleClientException :  {} ", ex.message, ex)
        return ResponseEntity(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}