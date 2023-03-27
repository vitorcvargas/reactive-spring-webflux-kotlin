package com.reactivespring.moviesreviewservice.validator

import com.reactivespring.moviesreviewservice.domain.Review
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator

@Component
class ReviewValidator : Validator {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun supports(clazz: Class<*>): Boolean {
        return Review::class.java == clazz
    }

    override fun validate(target: Any, errors: Errors) {
        ValidationUtils.rejectIfEmpty(errors, "movieInfoId", "movieInfoId.null", "Pass a valid movieInfoId")
        ValidationUtils.rejectIfEmpty(errors, "rating", "rating.null", "Pass a valid rating")
        val review: Review = target as Review
        log.info("Review : {}", review)
        if (review.rating < 0.0) {
            errors.rejectValue("rating", "rating.negative", "rating is negative and please pass a non-negative value")
        }
    }
}
