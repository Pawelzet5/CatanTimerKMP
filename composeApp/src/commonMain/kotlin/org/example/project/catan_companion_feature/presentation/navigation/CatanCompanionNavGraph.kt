package org.example.project.catan_companion_feature.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import org.example.project.catan_companion_feature.presentation.dashboard.DashboardScreenRoot
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigScreenRoot
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayScreenRoot
import org.example.project.catan_companion_feature.presentation.gameslist.GamesListScreenRoot
import org.example.project.catan_companion_feature.presentation.gamesummary.GameSummaryScreenRoot
import org.example.project.catan_companion_feature.presentation.playerdetails.PlayerDetailsScreenRoot
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListScreenRoot

fun NavGraphBuilder.catanCompanionGraph(
    navController: NavController,
) {
    navigation<DashboardRoute>(startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            DashboardScreenRoot(
                onNewGame = { navController.navigate(GameConfigRoute) },
                onResumeGame = { gameId -> navController.navigate(GameplayRoute(gameId)) },
                onGamesList = { navController.navigate(GamesListRoute) },
                onPlayersList = { navController.navigate(PlayersListRoute()) }
            )
        }
        composable<GameConfigRoute> {
            GameConfigScreenRoot(
                onNavigateBack = { navController.popBackStack() },
                onGameCreated = { gameId -> navController.navigate(GameplayRoute(gameId)) },
                onAddPlayer = { navController.navigate(PlayersListRoute(selectionMode = true)) }
            )
        }
        composable<GameplayRoute> { backStackEntry ->
            val route: GameplayRoute = backStackEntry.toRoute()
            GameplayScreenRoot(
                gameId = route.gameId,
                onNavigateToSummary = { gameId -> navController.navigate(GameSummaryRoute(gameId)) },
                onNavigateToWinnerSelection = { gameId -> navController.navigate(WinnerSelectionRoute(gameId)) },
                onNavigateToGameConfig = { navController.navigate(GameConfigRoute) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<PlayersListRoute> { backStackEntry ->
            val route: PlayersListRoute = backStackEntry.toRoute()
            PlayersListScreenRoot(
                isSelectionMode = route.selectionMode,
                onNavigateBack = { navController.popBackStack() },
                onPlayerClick = { playerId -> navController.navigate(PlayerDetailsRoute(playerId)) },
                onPlayersSelected = { navController.popBackStack() }
            )
        }
        composable<PlayerDetailsRoute> { backStackEntry ->
            val route: PlayerDetailsRoute = backStackEntry.toRoute()
            PlayerDetailsScreenRoot(
                playerId = route.playerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<GamesListRoute> {
            GamesListScreenRoot(
                onNavigateBack = { navController.popBackStack() },
                onGameClick = { gameId -> navController.navigate(GameSummaryRoute(gameId)) }
            )
        }
        composable<GameSummaryRoute> { backStackEntry ->
            val route: GameSummaryRoute = backStackEntry.toRoute()
            GameSummaryScreenRoot(
                gameId = route.gameId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<WinnerSelectionRoute> { backStackEntry ->
            val route: WinnerSelectionRoute = backStackEntry.toRoute()
            // WinnerSelectionScreenRoot(gameId = route.gameId, ...)
        }
    }
}
