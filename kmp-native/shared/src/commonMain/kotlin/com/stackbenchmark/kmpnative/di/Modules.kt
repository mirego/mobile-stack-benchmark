package com.stackbenchmark.kmpnative.di

import com.stackbenchmark.kmpnative.data.remote.ConfigReader
import com.stackbenchmark.kmpnative.data.remote.TmdbApiClient
import com.stackbenchmark.kmpnative.data.repository.MovieRepositoryImpl
import com.stackbenchmark.kmpnative.domain.repository.MovieRepository
import com.stackbenchmark.kmpnative.domain.usecase.GetMovieDetailUseCase
import com.stackbenchmark.kmpnative.domain.usecase.GetPopularMoviesUseCase
import com.stackbenchmark.kmpnative.presentation.moviedetail.MovieDetailViewModel
import com.stackbenchmark.kmpnative.presentation.movielist.MovieListViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val dataModule = module {
    single {
        TmdbApiClient(
            httpClient = get(),
            baseUrl = ConfigReader.baseUrl,
            apiKey = ConfigReader.apiKey
        )
    }
    single<MovieRepository> { MovieRepositoryImpl(apiClient = get()) }
}

val domainModule = module {
    factory { GetPopularMoviesUseCase(repository = get()) }
    factory { GetMovieDetailUseCase(repository = get()) }
}

val presentationModule = module {
    factory { MovieListViewModel(getPopularMoviesUseCase = get()) }
    factory { (movieId: Int) -> MovieDetailViewModel(getMovieDetailUseCase = get(), movieId = movieId) }
}

val sharedModules = listOf(platformModule, dataModule, domainModule, presentationModule)
