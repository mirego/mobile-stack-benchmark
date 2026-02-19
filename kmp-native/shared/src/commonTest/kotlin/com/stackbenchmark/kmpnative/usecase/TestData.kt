package com.stackbenchmark.kmpnative.usecase

import com.stackbenchmark.kmpnative.domain.entity.Genre
import com.stackbenchmark.kmpnative.domain.entity.Movie
import com.stackbenchmark.kmpnative.domain.entity.MovieDetail
import com.stackbenchmark.kmpnative.domain.entity.MoviePage

object TestData {
    fun movie(id: Int = 1, title: String = "Test Movie") = Movie(
        id = id,
        title = title,
        overview = "Test overview",
        posterPath = "/test.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 7.8,
        voteCount = 1200
    )

    fun moviePage(page: Int = 1, totalPages: Int = 5, movies: List<Movie>? = null) = MoviePage(
        movies = movies ?: listOf(movie(id = page * 100 + 1), movie(id = page * 100 + 2)),
        page = page,
        totalPages = totalPages
    )

    fun movieDetail(id: Int = 1) = MovieDetail(
        id = id,
        title = "Test Movie Detail",
        overview = "Detailed overview",
        posterPath = "/test.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 7.8,
        voteCount = 1200,
        tagline = "Test tagline",
        runtime = 142,
        genres = listOf(Genre(id = 28, name = "Action"), Genre(id = 12, name = "Adventure"))
    )
}
