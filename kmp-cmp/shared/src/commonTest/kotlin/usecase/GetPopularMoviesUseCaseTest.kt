package com.stackbenchmark.kmpcmp.usecase

import com.stackbenchmark.kmpcmp.FakeMovieRepository
import com.stackbenchmark.kmpcmp.createTestMovies
import com.stackbenchmark.kmpcmp.domain.entity.MoviePage
import com.stackbenchmark.kmpcmp.domain.usecase.GetPopularMoviesUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPopularMoviesUseCaseTest {

    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetPopularMoviesUseCase(fakeRepository)

    @Test
    fun getPopularMovies_success() = runTest {
        val movies = createTestMovies(5)
        val moviePage = MoviePage(movies = movies, page = 1, totalPages = 10)
        fakeRepository.popularMoviesResult = Result.success(moviePage)

        val result = useCase(page = 1)

        assertTrue(result.isSuccess)
        assertEquals(moviePage, result.getOrNull())
        assertEquals(1, fakeRepository.lastRequestedPage)
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
