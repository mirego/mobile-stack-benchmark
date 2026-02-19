package com.stackbenchmark.kmpnative.presentation.moviedetail

import com.stackbenchmark.kmpnative.domain.entity.MovieDetail

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val error: String? = null
)
