package com.stackbenchmark.kmpnative.presentation.moviedetail

import com.stackbenchmark.kmpnative.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val movieId: Int,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        _uiState.value = MovieDetailUiState(isLoading = true)
        scope.launch {
            getMovieDetailUseCase(id = movieId)
                .onSuccess { detail ->
                    _uiState.value = MovieDetailUiState(movieDetail = detail)
                }
                .onFailure { error ->
                    _uiState.value = MovieDetailUiState(
                        error = error.message ?: "An unknown error occurred"
                    )
                }
        }
    }

    fun retry() {
        if (_uiState.value.error != null) {
            loadDetail()
        }
    }
}
