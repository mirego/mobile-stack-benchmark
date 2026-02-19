package com.stackbenchmark.kmpnative.presentation

import com.stackbenchmark.kmpnative.domain.usecase.GetMovieDetailUseCase
import com.stackbenchmark.kmpnative.presentation.moviedetail.MovieDetailViewModel
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetMovieDetailUseCase(fakeRepository)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(movieId: Int = 1): MovieDetailViewModel {
        return MovieDetailViewModel(
            getMovieDetailUseCase = useCase,
            movieId = movieId,
            scope = testScope
        )
    }

    @Test
    fun initialState_isLoading() = testScope.runTest {
        fakeRepository.movieDetailResult = Result.success(TestData.movieDetail())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.movieDetail)
        assertNull(state.error)
    }

    @Test
    fun loadDetail_success() = testScope.runTest {
        val expected = TestData.movieDetail(id = 42)
        fakeRepository.movieDetailResult = Result.success(expected)

        val viewModel = createViewModel(movieId = 42)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.movieDetail)
        assertEquals(42, state.movieDetail!!.id)
        assertEquals("Test Movie Detail", state.movieDetail!!.title)
        assertNull(state.error)
    }

    @Test
    fun loadDetail_failure() = testScope.runTest {
        fakeRepository.movieDetailResult = Result.failure(RuntimeException("Network error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.movieDetail)
        assertEquals("Network error", state.error)
    }

    @Test
    fun retry_afterFailure() = testScope.runTest {
        fakeRepository.movieDetailResult = Result.failure(RuntimeException("Network error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.error)

        val expected = TestData.movieDetail()
        fakeRepository.movieDetailResult = Result.success(expected)
        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.movieDetail)
        assertNull(state.error)
    }

    @Test
    fun retry_afterSuccess_noOp() = testScope.runTest {
        val expected = TestData.movieDetail()
        fakeRepository.movieDetailResult = Result.success(expected)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.movieDetail)

        fakeRepository.movieDetailResult = Result.failure(RuntimeException("Should not be called"))
        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.movieDetail)
        assertNull(state.error)
    }
}
