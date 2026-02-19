package com.stackbenchmark.kmpcmp.usecase

import com.stackbenchmark.kmpcmp.FakeMovieRepository
import com.stackbenchmark.kmpcmp.domain.entity.Genre
import com.stackbenchmark.kmpcmp.domain.entity.MovieDetail
import com.stackbenchmark.kmpcmp.domain.usecase.GetMovieDetailUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMovieDetailUseCaseTest {

    private val fakeRepository = FakeMovieRepository()
    private val useCase = GetMovieDetailUseCase(fakeRepository)

    @Test
    fun getMovieDetail_success() = runTest {
        val detail = MovieDetail(
            id = 42,
            title = "Test Movie",
            overview = "An overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2024-06-15",
            voteAverage = 8.5,
            voteCount = 500,
            tagline = "A tagline",
            runtime = 142,
            genres = listOf(Genre(id = 28, name = "Action"))
        )
        fakeRepository.movieDetailResult = Result.success(detail)

        val result = useCase(id = 42)

        assertTrue(result.isSuccess)
        assertEquals(detail, result.getOrNull())
        assertEquals(42, fakeRepository.lastRequestedId)
    }

    @Test
    fun getMovieDetail_failure() = runTest {
        val exception = RuntimeException("Not found")
        fakeRepository.movieDetailResult = Result.failure(exception)

        val result = useCase(id = 99)

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
