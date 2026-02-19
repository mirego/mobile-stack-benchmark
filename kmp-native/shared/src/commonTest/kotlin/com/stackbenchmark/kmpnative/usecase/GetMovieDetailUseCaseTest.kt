package com.stackbenchmark.kmpnative.usecase

import com.stackbenchmark.kmpnative.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMovieDetailUseCaseTest {

    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetMovieDetailUseCase(fakeRepository)

    @Test
    fun getMovieDetail_success() = runTest {
        val expected = TestData.movieDetail()
        fakeRepository.movieDetailResult = Result.success(expected)

        val result = useCase(id = 1)

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun getMovieDetail_failure() = runTest {
        val exception = RuntimeException("Network error")
        fakeRepository.movieDetailResult = Result.failure(exception)

        val result = useCase(id = 1)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
