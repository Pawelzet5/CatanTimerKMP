package org.example.project.catan_companion_feature.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute

fun NavGraphBuilder.catanCompanionGraph(
    navController: NavController,
) {
    navigation<DashboardRoute>(startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            // DashboardScreen(...)
        }
        composable<GameConfigRoute> {
            // GameConfigScreen(...)
        }
        composable<GameplayRoute> { backStackEntry ->
            val route: GameplayRoute = backStackEntry.toRoute()
            // GameplayScreen(gameId = route.gameId, ...)
        }
        composable<PlayersListRoute> { backStackEntry ->
            val route: PlayersListRoute = backStackEntry.toRoute()
            // PlayersListScreen(selectionMode = route.selectionMode, ...)
        }
        composable<PlayerDetailsRoute> { backStackEntry ->
            val route: PlayerDetailsRoute = backStackEntry.toRoute()
            // PlayerDetailsScreen(playerId = route.playerId, ...)
        }
        composable<GamesListRoute> {
            // GamesListScreen(...)
        }
        composable<GameSummaryRoute> { backStackEntry ->
            val route: GameSummaryRoute = backStackEntry.toRoute()
            // GameSummaryScreen(gameId = route.gameId, ...)
        }
        composable<WinnerSelectionRoute> { backStackEntry ->
            val route: WinnerSelectionRoute = backStackEntry.toRoute()
            // WinnerSelectionScreen(gameId = route.gameId, ...)
        }
    }
}