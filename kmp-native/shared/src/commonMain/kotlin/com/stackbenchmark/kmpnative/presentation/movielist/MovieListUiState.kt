package com.stackbenchmark.kmpnative.presentation.movielist

import com.stackbenchmark.kmpnative.domain.entity.Movie

data class MovieListUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = Int.MAX_VALUE,
    val isLoadingMore: Boolean = false
)
