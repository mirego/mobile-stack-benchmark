package com.stackbenchmark.kmpcmp.domain.entity

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int
)

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

data class Genre(
    val id: Int,
    val name: String
)

data class MoviePage(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int
)
