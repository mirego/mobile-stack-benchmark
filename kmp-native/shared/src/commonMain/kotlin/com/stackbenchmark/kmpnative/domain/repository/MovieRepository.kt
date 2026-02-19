package com.stackbenchmark.kmpnative.domain.repository

import com.stackbenchmark.kmpnative.domain.entity.MovieDetail
import com.stackbenchmark.kmpnative.domain.entity.MoviePage

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): Result<MoviePage>
    suspend fun getMovieDetail(id: Int): Result<MovieDetail>
}
