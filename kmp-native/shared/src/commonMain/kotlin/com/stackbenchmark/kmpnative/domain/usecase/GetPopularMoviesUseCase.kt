package com.stackbenchmark.kmpnative.domain.usecase

import com.stackbenchmark.kmpnative.domain.entity.MoviePage
import com.stackbenchmark.kmpnative.domain.repository.MovieRepository

class GetPopularMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(page: Int): Result<MoviePage> =
        repository.getPopularMovies(page)
}
