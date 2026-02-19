package com.stackbenchmark.kmpnative.util

import com.stackbenchmark.kmpnative.presentation.moviedetail.MovieDetailUiState
import com.stackbenchmark.kmpnative.presentation.moviedetail.MovieDetailViewModel
import com.stackbenchmark.kmpnative.presentation.movielist.MovieListUiState
import com.stackbenchmark.kmpnative.presentation.movielist.MovieListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MovieListStateObserver(
    viewModel: MovieListViewModel,
    private val onChange: (MovieListUiState) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            viewModel.uiState.collect { state ->
                onChange(state)
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}

class MovieDetailStateObserver(
    viewModel: MovieDetailViewModel,
    private val onChange: (MovieDetailUiState) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            viewModel.uiState.collect { state ->
                onChange(state)
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
