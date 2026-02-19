package com.stackbenchmark.kmpnative.di

import com.stackbenchmark.kmpnative.presentation.moviedetail.MovieDetailViewModel
import com.stackbenchmark.kmpnative.presentation.movielist.MovieListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf

fun initKoin() {
    startKoin {
        modules(sharedModules)
    }
}

class KoinHelper : KoinComponent {
    fun getMovieListViewModel(): MovieListViewModel = get()
    fun getMovieDetailViewModel(movieId: Int): MovieDetailViewModel = get { parametersOf(movieId) }
}
