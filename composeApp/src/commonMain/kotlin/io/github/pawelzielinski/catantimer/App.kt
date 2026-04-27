package io.github.pawelzielinski.catantimer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.navigation.CatanCompanionNavGraph
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.navigation.catanCompanionGraph
import io.github.pawelzielinski.catantimer.core.designsystem.CatanTimerTheme

@Composable
fun App() {
    CatanTimerTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = CatanCompanionNavGraph
        ) {
            catanCompanionGraph(navController)
        }
    }
}
