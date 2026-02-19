package com.stackbenchmark.kmpcmp.domain.usecase

import com.stackbenchmark.kmpcmp.domain.entity.MoviePage
import com.stackbenchmark.kmpcmp.domain.repository.MovieRepository

class GetPopularMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(page: Int): Result<MoviePage> {
        return repository.getPopularMovies(page)
    }
}
