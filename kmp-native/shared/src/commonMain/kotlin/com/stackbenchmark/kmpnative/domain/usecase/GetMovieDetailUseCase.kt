package com.stackbenchmark.kmpnative.domain.usecase

import com.stackbenchmark.kmpnative.domain.entity.MovieDetail
import com.stackbenchmark.kmpnative.domain.repository.MovieRepository

class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(id: Int): Result<MovieDetail> =
        repository.getMovieDetail(id)
}
