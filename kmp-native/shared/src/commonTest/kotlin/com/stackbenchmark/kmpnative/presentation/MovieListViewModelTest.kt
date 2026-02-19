package com.stackbenchmark.kmpnative.presentation

import com.stackbenchmark.kmpnative.domain.usecase.GetPopularMoviesUseCase
import com.stackbenchmark.kmpnative.presentation.movielist.MovieListViewModel
import com.stackbenchmark.kmpnative.usecase.FakeMovieRepository
import com.stackbenchmark.kmpnative.usecase.TestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetPopularMoviesUseCase(fakeRepository)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MovieListViewModel {
        return MovieListViewModel(
            getPopularMoviesUseCase = useCase,
            scope = testScope
        )
    }

    @Test
    fun loadFirstPage_success() = testScope.runTest {
        val page = TestData.moviePage(page = 1, totalPages = 5)
        fakeRepository.popularMoviesResult = Result.success(page)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.movies.size)
        assertEquals(1, state.currentPage)
        assertEquals(5, state.totalPages)
        assertNull(state.error)
    }

    @Test
    fun loadFirstPage_failure() = testScope.runTest {
        fakeRepository.popularMoviesResult = Result.failure(RuntimeException("Network error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.movies.isEmpty())
        assertEquals("Network error", state.error)
    }

    @Test
    fun loadNextPage_appendsMovies() = testScope.runTest {
        val page1 = TestData.moviePage(page = 1, totalPages = 5)
        fakeRepository.popularMoviesResult = Result.success(page1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val page2 = TestData.moviePage(page = 2, totalPages = 5)
        fakeRepository.popularMoviesResult = Result.success(page2)
        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.movies.size)
        assertEquals(2, state.currentPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun loadNextPage_noOpAtEnd() = testScope.runTest {
        val page = TestData.moviePage(page = 1, totalPages = 1)
        fakeRepository.popularMoviesResult = Result.success(page)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.currentPage)
        assertEquals(2, state.movies.size)
    }

    @Test
    fun retry_afterFailure() = testScope.runTest {
        fakeRepository.popularMoviesResult = Result.failure(RuntimeException("Network error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.error)

        val page = TestData.moviePage(page = 1, totalPages = 5)
        fakeRepository.popularMoviesResult = Result.success(page)
        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.movies.size)
        assertNull(state.error)
    }

    @Test
    fun retry_afterSuccess_noOp() = testScope.runTest {
        val page = TestData.moviePage(page = 1, totalPages = 5)
        fakeRepository.popularMoviesResult = Result.success(page)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.movies.size)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun loadFirstPage_resetsState() = testScope.runTest {
        val page1 = TestData.moviePage(page = 1, totalPages = 5)
        fakeRepository.popularMoviesResult = Result.success(page1)

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.movies.size)

        val newPage = TestData.moviePage(
            page = 1,
            totalPages = 3,
            movies = listOf(TestData.movie(id = 999, title = "New Movie"))
        )
        fakeRepository.popularMoviesResult = Result.success(newPage)
        viewModel.loadFirstPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.movies.size)
        assertEquals("New Movie", state.movies[0].title)
    }
}
