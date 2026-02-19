package com.stackbenchmark.kmpnative.domain.entity

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val tagline: String?,
    val runtime: Int?,
    val genres: List<Genre>
)
