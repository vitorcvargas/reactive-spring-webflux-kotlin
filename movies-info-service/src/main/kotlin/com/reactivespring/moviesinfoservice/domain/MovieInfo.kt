package com.reactivespring.moviesinfoservice.domain

import com.fasterxml.jackson.annotation.JsonCreator
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.validation.annotation.Validated
import java.time.LocalDate

@Document
@Validated
data class MovieInfo (
    @Id
    var movieInfoId: String?,

    @get:NotBlank(message = "movieInfo.name must be present")
    var name: String,

    @get:NotNull
    @get:Positive(message = "movieInfo.year must be a Positive Value")
    var year: Int,

    @field:Valid
    @field:NotEmpty(message = "movieInfo.cast must be present")
    var cast: List<Actor>,

    var release_date: LocalDate
)

data class Actor (
    @NotBlank
    var name: String
)