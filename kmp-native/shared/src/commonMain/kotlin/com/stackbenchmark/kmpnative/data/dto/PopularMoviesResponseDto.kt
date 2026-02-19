package com.stackbenchmark.kmpnative.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PopularMoviesResponseDto(
    val page: Int,
    @SerialName("total_pages") val totalPages: Int,
    val results: List<MovieDto>
)
