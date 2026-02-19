package com.stackbenchmark.kmpnative.data.remote

import com.stackbenchmark.kmpnative.data.dto.MovieDetailDto
import com.stackbenchmark.kmpnative.data.dto.PopularMoviesResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class TmdbApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val apiKey: String
) {
    suspend fun getPopularMovies(page: Int): PopularMoviesResponseDto {
        return httpClient.get("$baseUrl/movie/popular") {
            parameter("api_key", apiKey)
            parameter("page", page)
        }.body()
    }

    suspend fun getMovieDetail(movieId: Int): MovieDetailDto {
        return httpClient.get("$baseUrl/movie/$movieId") {
            parameter("api_key", apiKey)
        }.body()
    }
}
