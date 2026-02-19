package com.stackbenchmark.kmpcmp.presentation

import com.stackbenchmark.kmpcmp.FakeMovieRepository
import com.stackbenchmark.kmpcmp.createTestMovies
import com.stackbenchmark.kmpcmp.domain.entity.MoviePage
import com.stackbenchmark.kmpcmp.domain.usecase.GetPopularMoviesUseCase
import com.stackbenchmark.kmpcmp.presentation.movielist.MovieListUiState
import com.stackbenchmark.kmpcmp.presentation.movielist.MovieListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetPopularMoviesUseCase(fakeRepository)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val viewModel = MovieListViewModel(useCase, testScope)

    @Test
    fun initialState() {
        val state = viewModel.uiState.value
        assertEquals(MovieListUiState(), state)
        assertFalse(state.isLoading)
        assertTrue(state.movies.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun loadFirstPage_success() = testScope.runTest {
        val movies = createTestMovies(5)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = movies, page = 1, totalPages = 10)
        )

        viewModel.loadFirstPage()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(5, state.movies.size)
        assertEquals(1, state.currentPage)
        assertEquals(10, state.totalPages)
        assertNull(state.error)
    }

    @Test
    fun loadFirstPage_failure() = testScope.runTest {
        fakeRepository.popularMoviesResult = Result.failure(RuntimeException("Network error"))

        viewModel.loadFirstPage()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.movies.isEmpty())
        assertEquals("Network error", state.error)
    }

    @Test
    fun loadNextPage_appendsMovies() = testScope.runTest {
        val page1Movies = createTestMovies(5, startId = 1)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = page1Movies, page = 1, totalPages = 10)
        )
        viewModel.loadFirstPage()

        val page2Movies = createTestMovies(5, startId = 6)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = page2Movies, page = 2, totalPages = 10)
        )
        viewModel.loadNextPage()

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingMore)
        assertEquals(10, state.movies.size)
        assertEquals(2, state.currentPage)
        assertEquals(1, state.movies.first().id)
        assertEquals(10, state.movies.last().id)
    }

    @Test
    fun loadNextPage_noOpAtEnd() = testScope.runTest {
        val movies = createTestMovies(5)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = movies, page = 3, totalPages = 3)
        )
        viewModel.loadFirstPage()
        fakeRepository.getPopularMoviesCallCount = 0

        viewModel.loadNextPage()

        assertEquals(0, fakeRepository.getPopularMoviesCallCount)
        assertEquals(5, viewModel.uiState.value.movies.size)
    }

    @Test
    fun retry_afterFailure() = testScope.runTest {
        fakeRepository.popularMoviesResult = Result.failure(RuntimeException("Network error"))
        viewModel.loadFirstPage()
        assertTrue(viewModel.uiState.value.error != null)

        val movies = createTestMovies(5)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = movies, page = 1, totalPages = 10)
        )
        viewModel.retry()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(5, state.movies.size)
        assertNull(state.error)
    }

    @Test
    fun retry_afterSuccess_noOp() = testScope.runTest {
        val movies = createTestMovies(5)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = movies, page = 1, totalPages = 10)
        )
        viewModel.loadFirstPage()
        fakeRepository.getPopularMoviesCallCount = 0

        viewModel.retry()

        assertEquals(0, fakeRepository.getPopularMoviesCallCount)
    }

    @Test
    fun loadFirstPage_resetsState() = testScope.runTest {
        val page1Movies = createTestMovies(5, startId = 1)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = page1Movies, page = 1, totalPages = 10)
        )
        viewModel.loadFirstPage()
        assertEquals(5, viewModel.uiState.value.movies.size)

        val newMovies = createTestMovies(3, startId = 100)
        fakeRepository.popularMoviesResult = Result.success(
            MoviePage(movies = newMovies, page = 1, totalPages = 5)
        )
        viewModel.loadFirstPage()

        val state = viewModel.uiState.value
        assertEquals(3, state.movies.size)
        assertEquals(100, state.movies.first().id)
        assertEquals(1, state.currentPage)
        assertEquals(5, state.totalPages)
    }
}
