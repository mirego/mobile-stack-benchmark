package com.stackbenchmark.kmpcmp.presentation.moviedetail

import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movieDetail: MovieDetail? = null,
    val error: String? = null
)
