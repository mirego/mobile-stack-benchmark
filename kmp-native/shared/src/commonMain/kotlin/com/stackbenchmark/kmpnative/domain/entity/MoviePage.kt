package com.stackbenchmark.kmpnative.domain.entity

data class MoviePage(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int
)
