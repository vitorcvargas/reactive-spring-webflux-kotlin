package com.reactivespring.moviesinfoservice.exceptionhandler

import com.reactivespring.moviesinfoservice.exception.MovieInfoNotfoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import java.util.stream.Collectors

@ControllerAdvice
class GlobalErrorHandler {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleRequestBodyError(ex: WebExchangeBindException): ResponseEntity<String> {
        log.error("Exception caught in handleRequestBodyError :  {} ", ex.message, ex)
        val error = ex.bindingResult.allErrors.stream()
            .map { obj -> obj.defaultMessage }
            .sorted()
            .collect(Collectors.joining(","))
        log.error("errorList : {}", error)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(MovieInfoNotfoundException::class)
    fun handleMovieInfoNotfoundException(ex: MovieInfoNotfoundException): ResponseEntity<String> {
        log.error("Exception caught in handleMovieInfoNotfoundException :  {} ", ex.message, ex)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)
    }
}