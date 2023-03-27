package com.reactivespring.moviesservice.util

import com.reactivespring.moviesservice.exception.MoviesInfoServerException
import com.reactivespring.moviesservice.exception.ReviewsServerException
import reactor.core.Exceptions
import reactor.util.retry.Retry
import reactor.util.retry.Retry.RetrySignal
import reactor.util.retry.RetryBackoffSpec
import reactor.util.retry.RetrySpec
import java.time.Duration

object RetryUtil {
    fun retrySpec(): Retry {
        return RetrySpec.fixedDelay(3, Duration.ofSeconds(1))
            .filter { ex: Throwable? -> ex is MoviesInfoServerException || ex is ReviewsServerException }
            .onRetryExhaustedThrow { _: RetryBackoffSpec?, retrySignal: RetrySignal ->
                Exceptions.propagate(
                    retrySignal.failure()
                )
            }
    }
}