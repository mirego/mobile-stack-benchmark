package com.stackbenchmark.kmpcmp.domain.usecase

import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.repository.MovieRepository

class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(id: Int): Result<MovieDetail> {
        return repository.getMovieDetail(id)
    }
}
