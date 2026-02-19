package com.stackbenchmark.kmpcmp.presentation.moviedetail

import com.stackbenchmark.kmpcmp.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private var currentMovieId: Int? = null

    fun loadDetail(id: Int) {
        currentMovieId = id
        _uiState.update { MovieDetailUiState(isLoading = true) }
        scope.launch {
            getMovieDetailUseCase(id)
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movieDetail = detail,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "An unknown error occurred"
                        )
                    }
                }
        }
    }

    fun retry() {
        val id = currentMovieId ?: return
        if (_uiState.value.error == null) return
        loadDetail(id)
    }
}
