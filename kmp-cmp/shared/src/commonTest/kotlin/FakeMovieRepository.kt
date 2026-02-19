package com.stackbenchmark.kmpcmp

import com.stackbenchmark.kmpcmp.domain.entity.Genre
import com.stackbenchmark.kmpcmp.domain.entity.Movie
import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.entity.MoviePage
import com.stackbenchmark.kmpcmp.domain.repository.MovieRepository

class FakeMovieRepository : MovieRepository {
    var popularMoviesResult: Result<MoviePage> = Result.success(
        MoviePage(movies = emptyList(), page = 1, totalPages = 1)
    )
    var movieDetailResult: Result<MovieDetail> = Result.success(
        MovieDetail(
            id = 1,
            title = "Test Movie",
            overview = "Test overview",
            posterPath = null,
            backdropPath = null,
            releaseDate = "2024-01-01",
            voteAverage = 7.5,
            voteCount = 100,
            tagline = "Test tagline",
            runtime = 120,
            genres = listOf(Genre(id = 1, name = "Action"))
        )
    )
    var getPopularMoviesCallCount = 0
    var getMovieDetailCallCount = 0
    var lastRequestedPage: Int? = null
    var lastRequestedId: Int? = null

    override suspend fun getPopularMovies(page: Int): Result<MoviePage> {
        getPopularMoviesCallCount++
        lastRequestedPage = page
        return popularMoviesResult
    }

    override suspend fun getMovieDetail(id: Int): Result<MovieDetail> {
        getMovieDetailCallCount++
        lastRequestedId = id
        return movieDetailResult
    }
}

fun createTestMovies(count: Int, startId: Int = 1): List<Movie> {
    return (startId until startId + count).map { id ->
        Movie(
            id = id,
            title = "Movie $id",
            overview = "Overview $id",
            posterPath = "/poster$id.jpg",
            backdropPath = "/backdrop$id.jpg",
            releaseDate = "2024-01-${id.toString().padStart(2, '0')}",
            voteAverage = 7.0 + (id % 10) * 0.1,
            voteCount = 100 * id
        )
    }
}
