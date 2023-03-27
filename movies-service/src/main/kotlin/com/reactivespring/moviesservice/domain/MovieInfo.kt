package com.reactivespring.moviesservice.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class MovieInfo(
    var movieInfoId: String?,

    @get:NotBlank(message = "movieInfo.name must be present")
    var name: String?,

    @get:NotNull
    @get:Positive(message = "movieInfo.year must be a Positive Value")
    var year: Int?,

    @get:NotNull
    var cast: List<String>?,

    var release_date: LocalDate?
)