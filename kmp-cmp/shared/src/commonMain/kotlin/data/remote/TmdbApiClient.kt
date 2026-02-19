package com.stackbenchmark.kmpcmp.data.remote

import com.stackbenchmark.kmpcmp.data.dto.MovieDetailDto
import com.stackbenchmark.kmpcmp.data.dto.PopularMoviesResponseDto
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

    suspend fun getMovieDetail(id: Int): MovieDetailDto {
        return httpClient.get("$baseUrl/movie/$id") {
            parameter("api_key", apiKey)
        }.body()
    }
}
