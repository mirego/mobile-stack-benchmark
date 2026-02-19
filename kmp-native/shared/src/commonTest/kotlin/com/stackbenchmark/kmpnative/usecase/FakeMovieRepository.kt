package com.stackbenchmark.kmpnative.usecase

import com.stackbenchmark.kmpnative.domain.entity.MovieDetail
import com.stackbenchmark.kmpnative.domain.entity.MoviePage
import com.stackbenchmark.kmpnative.domain.repository.MovieRepository

class FakeMovieRepository : MovieRepository {
    var popularMoviesResult: Result<MoviePage> = Result.failure(RuntimeException("Not configured"))
    var movieDetailResult: Result<MovieDetail> = Result.failure(RuntimeException("Not configured"))

    override suspend fun getPopularMovies(page: Int): Result<MoviePage> = popularMoviesResult
    override suspend fun getMovieDetail(id: Int): Result<MovieDetail> = movieDetailResult
}
