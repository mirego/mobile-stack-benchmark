package com.stackbenchmark.kmpcmp.domain.repository

import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.entity.MoviePage

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): Result<MoviePage>
    suspend fun getMovieDetail(id: Int): Result<MovieDetail>
}
