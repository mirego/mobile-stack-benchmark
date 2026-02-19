package com.stackbenchmark.kmpnative.usecase

import com.stackbenchmark.kmpnative.domain.usecase.GetPopularMoviesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPopularMoviesUseCaseTest {

    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetPopularMoviesUseCase(fakeRepository)

    @Test
    fun getPopularMovies_success() = runTest {
        val expected = TestData.moviePage()
        fakeRepository.popularMoviesResult = Result.success(expected)

        val result = useCase(page = 1)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun getPopularMovies_failure() = runTest {
        val exception = RuntimeException("Network error")
        fakeRepository.popularMoviesResult = Result.failure(exception)

        val result = useCase(page = 1)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
