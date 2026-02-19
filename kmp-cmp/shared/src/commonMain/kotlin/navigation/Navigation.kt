package com.stackbenchmark.kmpcmp.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.stackbenchmark.kmpcmp.domain.usecase.GetMovieDetailUseCase
import com.stackbenchmark.kmpcmp.domain.usecase.GetPopularMoviesUseCase
import com.stackbenchmark.kmpcmp.presentation.moviedetail.MovieDetailViewModel
import com.stackbenchmark.kmpcmp.presentation.movielist.MovieListViewModel
import com.stackbenchmark.kmpcmp.ui.moviedetail.MovieDetailScreen
import com.stackbenchmark.kmpcmp.ui.movielist.MovieListScreen
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

private object AppKoinComponent : KoinComponent

private sealed class Screen {
    data object MovieList : Screen()
    data class Detail(val movieId: Int) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.MovieList) }

    val listViewModel = remember {
        MovieListViewModel(
            getPopularMoviesUseCase = AppKoinComponent.get<GetPopularMoviesUseCase>(),
            scope = scope
        )
    }

    when (val screen = currentScreen) {
        is Screen.MovieList -> {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("KMP-CMP") })
                }
            ) { padding ->
                MovieListScreen(
                    viewModel = listViewModel,
                    onMovieClick = { movieId ->
                        currentScreen = Screen.Detail(movieId)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        is Screen.Detail -> {
            val detailViewModel = remember(screen.movieId) {
                MovieDetailViewModel(
                    getMovieDetailUseCase = AppKoinComponent.get<GetMovieDetailUseCase>(),
                    scope = scope
                )
            }

            MovieDetailScreen(
                viewModel = detailViewModel,
                movieId = screen.movieId,
                onBackClick = {
                    currentScreen = Screen.MovieList
                }
            )
        }
    }
}
