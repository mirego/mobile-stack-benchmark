package com.stackbenchmark.kmpcmp.data.repository

import com.stackbenchmark.kmpcmp.data.mapper.toDomain
import com.stackbenchmark.kmpcmp.data.remote.TmdbApiClient
import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.entity.MoviePage
import com.stackbenchmark.kmpcmp.domain.repository.MovieRepository

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
