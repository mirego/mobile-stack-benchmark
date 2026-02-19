package com.stackbenchmark.kmpnative.android

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stackbenchmark.kmpnative.di.sharedModules
import com.stackbenchmark.kmpnative.presentation.moviedetail.MovieDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin

class KmpNativeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KmpNativeApp)
            modules(sharedModules)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "movieList") {
        composable("movieList") {
            Scaffold(
                topBar = {
                    TopAppBar(title = { Text("KMP-Native") })
                }
            ) { padding ->
                Surface(modifier = Modifier.padding(padding)) {
                    MovieListScreen(onMovieClick = { movieId ->
                        navController.navigate("movieDetail/$movieId")
                    })
                }
            }
        }
        composable(
            "movieDetail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            val viewModel = remember(movieId) {
                getKoin().get<MovieDetailViewModel> { parametersOf(movieId) }
            }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("KMP-Native") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Surface(modifier = Modifier.padding(padding)) {
                    MovieDetailScreen(viewModel = viewModel)
                }
            }
        }
    }
}
