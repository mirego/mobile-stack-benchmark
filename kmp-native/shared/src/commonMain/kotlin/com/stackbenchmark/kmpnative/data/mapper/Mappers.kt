package com.stackbenchmark.kmpnative.data.mapper

import com.stackbenchmark.kmpnative.data.dto.GenreDto
import com.stackbenchmark.kmpnative.data.dto.MovieDetailDto
import com.stackbenchmark.kmpnative.data.dto.MovieDto
import com.stackbenchmark.kmpnative.data.dto.PopularMoviesResponseDto
import com.stackbenchmark.kmpnative.domain.entity.Genre
import com.stackbenchmark.kmpnative.domain.entity.Movie
import com.stackbenchmark.kmpnative.domain.entity.MovieDetail
import com.stackbenchmark.kmpnative.domain.entity.MoviePage

fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount
)

fun MovieDetailDto.toDomain(): MovieDetail = MovieDetail(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    tagline = tagline,
    runtime = runtime,
    genres = genres.map { it.toDomain() }
)

fun GenreDto.toDomain(): Genre = Genre(
    id = id,
    name = name
)

fun PopularMoviesResponseDto.toDomain(): MoviePage = MoviePage(
    movies = results.map { it.toDomain() },
    page = page,
    totalPages = totalPages
)
