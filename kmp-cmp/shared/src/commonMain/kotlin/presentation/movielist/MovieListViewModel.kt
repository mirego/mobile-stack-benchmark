package com.stackbenchmark.kmpcmp.presentation.movielist

import com.stackbenchmark.kmpcmp.domain.entity.Movie
import com.stackbenchmark.kmpcmp.domain.usecase.GetPopularMoviesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieListUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = Int.MAX_VALUE,
    val isLoadingMore: Boolean = false
)

class MovieListViewModel(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(MovieListUiState())
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    private enum class LastAction { LOAD_FIRST_PAGE, LOAD_NEXT_PAGE }
    private var lastAction: LastAction? = null

    fun loadFirstPage() {
        lastAction = LastAction.LOAD_FIRST_PAGE
        _uiState.update { MovieListUiState(isLoading = true) }
        scope.launch {
            getPopularMoviesUseCase(page = 1)
                .onSuccess { moviePage ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movies = moviePage.movies,
                            currentPage = moviePage.page,
                            totalPages = moviePage.totalPages,
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

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.currentPage >= state.totalPages) return

        lastAction = LastAction.LOAD_NEXT_PAGE
        val nextPage = state.currentPage + 1
        _uiState.update { it.copy(isLoadingMore = true) }

        scope.launch {
            getPopularMoviesUseCase(page = nextPage)
                .onSuccess { moviePage ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            movies = it.movies + moviePage.movies,
                            currentPage = moviePage.page,
                            totalPages = moviePage.totalPages,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error = error.message ?: "An unknown error occurred"
                        )
                    }
                }
        }
    }

    fun retry() {
        if (_uiState.value.error == null) return
        when (lastAction) {
            LastAction.LOAD_FIRST_PAGE -> loadFirstPage()
            LastAction.LOAD_NEXT_PAGE -> loadNextPage()
            null -> {}
        }
    }
}
