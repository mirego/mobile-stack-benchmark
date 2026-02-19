package com.stackbenchmark.kmpcmp.di

import com.stackbenchmark.kmpcmp.config.Config
import com.stackbenchmark.kmpcmp.data.remote.TmdbApiClient
import com.stackbenchmark.kmpcmp.data.repository.MovieRepositoryImpl
import com.stackbenchmark.kmpcmp.domain.repository.MovieRepository
import com.stackbenchmark.kmpcmp.domain.usecase.GetMovieDetailUseCase
import com.stackbenchmark.kmpcmp.domain.usecase.GetPopularMoviesUseCase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    single { TmdbApiClient(get(), Config.baseUrl, Config.apiKey) }
    single<MovieRepository> { MovieRepositoryImpl(get()) }

    factory { GetPopularMoviesUseCase(get()) }
    factory { GetMovieDetailUseCase(get()) }
}

private var koinStarted = false

fun initKoin() {
    if (koinStarted) return
    koinStarted = true
    startKoin {
        modules(appModule)
    }
}
