package com.reactivespring.moviesinfoservice.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Document
@Validated
data class MovieInfo(
    @Id
    var movieInfoId: String,

    @NotBlank(message = "movieInfo.name must be present")
    var name: String,

    @NotNull
    @Positive(message = "movieInfo.year must be a Positive Value")
    var year: Int,

    @NotNull
    var cast: List<String>,

    var release_date: LocalDate
)