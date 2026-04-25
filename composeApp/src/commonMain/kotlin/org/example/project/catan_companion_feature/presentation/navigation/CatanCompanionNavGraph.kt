package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.dashboard.DashboardScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameconfig.GameConfigScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay.GameplayScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist.GamesListScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gamesummary.GameSummaryScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.winnerselection.WinnerSelectionScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerdetails.PlayerDetailsScreenRoot
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerslist.PlayersListScreenRoot

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
                onResumeGame = { gameId -> navController.navigate(GameplayRoute(gameId)) },
                onGameSummary = { gameId -> navController.navigate(GameSummaryRoute(gameId)) }
            )
        }
        composable<WinnerSelectionRoute> { backStackEntry ->
            val route: WinnerSelectionRoute = backStackEntry.toRoute()
            WinnerSelectionScreenRoot(
                gameId = route.gameId,
                onGameFinished = { gameId ->
                    navController.navigate(GameSummaryRoute(gameId)) {
                        popUpTo<DashboardRoute>()
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<GameSummaryRoute> { backStackEntry ->
            val route: GameSummaryRoute = backStackEntry.toRoute()
            GameSummaryScreenRoot(
                gameId = route.gameId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<DashboardRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}
