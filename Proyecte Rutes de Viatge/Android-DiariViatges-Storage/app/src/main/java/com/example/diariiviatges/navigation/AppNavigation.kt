package com.example.diariiviatges.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.diariiviatges.ui.FormScreen
import com.example.diariiviatges.ui.HistoryScreen
import com.example.diariiviatges.ui.PostalScreen
import com.example.diariiviatges.viewmodel.TravelDiaryViewModel

/** Rutes de Navigation Compose. */
object Routes {
    const val FORM = "form"
    const val POSTAL = "postal"
    const val HISTORY = "history"
}

/**
 * Graf de navegació. El ViewModel és compartit per les tres pantalles
 * (una única instància ambViewModel()), de manera que la ruta creada al formulari
 * està disponible a PostalScreen sense necessitat d'arguments. Alternatives vegeu README.
 */
@Composable
fun AppNavigation(viewModel: TravelDiaryViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.FORM,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        composable(Routes.FORM) {
            FormScreen(
                viewModel = viewModel,
                onNavigateToPostal = { navController.navigate(Routes.POSTAL) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.POSTAL) {
            PostalScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onEntrySelected = { entry ->
                    viewModel.onEntrySelected(entry)
                    navController.navigate(Routes.POSTAL)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
