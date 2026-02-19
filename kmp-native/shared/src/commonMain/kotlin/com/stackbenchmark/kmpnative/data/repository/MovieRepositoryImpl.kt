package com.stackbenchmark.kmpnative.data.repository

import com.stackbenchmark.kmpnative.data.mapper.toDomain
import com.stackbenchmark.kmpnative.data.remote.TmdbApiClient
import com.stackbenchmark.kmpnative.domain.entity.MovieDetail
import com.stackbenchmark.kmpnative.domain.entity.MoviePage
import com.stackbenchmark.kmpnative.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val apiClient: TmdbApiClient
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): Result<MoviePage> {
        return try {
            val response = apiClient.getPopularMovies(page)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMovieDetail(id: Int): Result<MovieDetail> {
        return try {
            val response = apiClient.getMovieDetail(id)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
