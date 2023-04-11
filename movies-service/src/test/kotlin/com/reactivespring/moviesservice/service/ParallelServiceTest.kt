package com.reactivespring.moviesservice.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ParallelServiceTest {

    @Autowired
    lateinit var parallelService: ParallelService

    @Test
    fun shouldDoSimultaneousProcessing() {
        parallelService.doSimultaneousProcessing(listOf(1,2,3,4,5))
    }

    @Test
    fun singleThread() {
        parallelService.doSingleThreadProcessing(listOf(1,2,3,4,5))
    }
}