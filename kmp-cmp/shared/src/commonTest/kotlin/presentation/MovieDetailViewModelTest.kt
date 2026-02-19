package com.stackbenchmark.kmpcmp.presentation

import com.stackbenchmark.kmpcmp.FakeMovieRepository
import com.stackbenchmark.kmpcmp.domain.entity.Genre
import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.usecase.GetMovieDetailUseCase
import com.stackbenchmark.kmpcmp.presentation.moviedetail.MovieDetailUiState
import com.stackbenchmark.kmpcmp.presentation.moviedetail.MovieDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetMovieDetailUseCase(fakeRepository)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val viewModel = MovieDetailViewModel(useCase, testScope)

    @Test
    fun initialState() {
        val state = viewModel.uiState.value
        assertEquals(MovieDetailUiState(), state)
        assertFalse(state.isLoading)
        assertNull(state.movieDetail)
        assertNull(state.error)
    }

    @Test
    fun loadDetail_success() = testScope.runTest {
        val detail = MovieDetail(
            id = 42,
            title = "Test Movie",
            overview = "A great movie",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-06-15",
            voteAverage = 8.5,
            voteCount = 500,
            tagline = "Best movie ever",
            runtime = 142,
            genres = listOf(Genre(id = 1, name = "Action"), Genre(id = 2, name = "Drama"))
        )
        fakeRepository.movieDetailResult = Result.success(detail)

        viewModel.loadDetail(42)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.movieDetail)
        assertEquals(42, state.movieDetail!!.id)
        assertEquals("Test Movie", state.movieDetail!!.title)
        assertEquals("Best movie ever", state.movieDetail!!.tagline)
        assertEquals(142, state.movieDetail!!.runtime)
        assertEquals(2, state.movieDetail!!.genres.size)
        assertNull(state.error)
        assertEquals(42, fakeRepository.lastRequestedId)
    }

    @Test
    fun loadDetail_failure() = testScope.runTest {
        fakeRepository.movieDetailResult = Result.failure(RuntimeException("Network error"))

        viewModel.loadDetail(42)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.movieDetail)
        assertEquals("Network error", state.error)
    }

    @Test
    fun retry_afterFailure() = testScope.runTest {
        fakeRepository.movieDetailResult = Result.failure(RuntimeException("Network error"))
        viewModel.loadDetail(42)
        assertTrue(viewModel.uiState.value.error != null)

        val detail = MovieDetail(
            id = 42,
            title = "Test Movie",
            overview = "A great movie",
            posterPath = null,
            backdropPath = null,
            releaseDate = "2024-06-15",
            voteAverage = 8.5,
            voteCount = 500,
            tagline = null,
            runtime = 120,
            genres = listOf(Genre(id = 1, name = "Action"))
        )
        fakeRepository.movieDetailResult = Result.success(detail)

        viewModel.retry()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.movieDetail)
        assertEquals(42, state.movieDetail!!.id)
        assertNull(state.error)
    }

    @Test
    fun retry_afterSuccess_noOp() = testScope.runTest {
        val detail = MovieDetail(
            id = 42,
            title = "Test Movie",
            overview = "A great movie",
            posterPath = null,
            backdropPath = null,
            releaseDate = "2024-06-15",
            voteAverage = 8.5,
            voteCount = 500,
            tagline = null,
            runtime = 120,
            genres = emptyList()
        )
        fakeRepository.movieDetailResult = Result.success(detail)
        viewModel.loadDetail(42)
        fakeRepository.getMovieDetailCallCount = 0

        viewModel.retry()

        assertEquals(0, fakeRepository.getMovieDetailCallCount)
    }
}
