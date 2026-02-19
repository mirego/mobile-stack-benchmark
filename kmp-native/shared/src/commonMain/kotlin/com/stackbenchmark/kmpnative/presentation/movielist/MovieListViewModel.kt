package com.stackbenchmark.kmpnative.presentation.movielist

import com.stackbenchmark.kmpnative.domain.usecase.GetPopularMoviesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieListViewModel(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(MovieListUiState())
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    private enum class LastAction { FIRST_PAGE, NEXT_PAGE }
    private var lastAction: LastAction? = null

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        lastAction = LastAction.FIRST_PAGE
        _uiState.value = MovieListUiState(isLoading = true)
        scope.launch {
            getPopularMoviesUseCase(page = 1)
                .onSuccess { moviePage ->
                    _uiState.value = MovieListUiState(
                        movies = moviePage.movies,
                        currentPage = moviePage.page,
                        totalPages = moviePage.totalPages
                    )
                }
                .onFailure { error ->
                    _uiState.value = MovieListUiState(
                        error = error.message ?: "An unknown error occurred"
                    )
                }
        }
    }

    fun loadNextPage() {
        val current = _uiState.value
        if (current.isLoading || current.isLoadingMore) return
        if (current.currentPage >= current.totalPages) return

        lastAction = LastAction.NEXT_PAGE
        _uiState.value = current.copy(isLoadingMore = true)
        val nextPage = current.currentPage + 1
        scope.launch {
            getPopularMoviesUseCase(page = nextPage)
                .onSuccess { moviePage ->
                    val updated = _uiState.value
                    _uiState.value = updated.copy(
                        movies = updated.movies + moviePage.movies,
                        currentPage = moviePage.page,
                        totalPages = moviePage.totalPages,
                        isLoadingMore = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = error.message ?: "An unknown error occurred"
                    )
                }
        }
    }

    fun retry() {
        val current = _uiState.value
        if (current.error == null) return

        when (lastAction) {
            LastAction.FIRST_PAGE -> loadFirstPage()
            LastAction.NEXT_PAGE -> {
                _uiState.value = current.copy(error = null)
                loadNextPage()
            }
            null -> loadFirstPage()
        }
    }
}
