package com.stackbenchmark.kmpcmp.data.mapper

import com.stackbenchmark.kmpcmp.data.dto.GenreDto
import com.stackbenchmark.kmpcmp.data.dto.MovieDetailDto
import com.stackbenchmark.kmpcmp.data.dto.MovieDto
import com.stackbenchmark.kmpcmp.data.dto.PopularMoviesResponseDto
import com.stackbenchmark.kmpcmp.domain.entity.Genre
import com.stackbenchmark.kmpcmp.domain.entity.Movie
import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.entity.MoviePage

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

fun GenreDto.toDomain(): Genre = Genre(
    id = id,
    name = name
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

fun PopularMoviesResponseDto.toDomain(): MoviePage = MoviePage(
    movies = results.map { it.toDomain() },
    page = page,
    totalPages = totalPages
)
