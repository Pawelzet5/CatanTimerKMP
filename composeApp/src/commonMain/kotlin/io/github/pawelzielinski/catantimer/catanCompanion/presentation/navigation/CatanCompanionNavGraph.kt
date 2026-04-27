package io.github.pawelzielinski.catantimer.catanCompanion.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard.DashboardScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig.GameConfigAction
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig.GameConfigScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig.GameConfigViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig.PlayersSelectionViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay.GameplayScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameslist.GamesListScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary.GameSummaryScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerdetails.PlayerDetailsScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerslist.PlayersListScreenRoot
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.winnerselection.WinnerSelectionScreenRoot
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.catanCompanionGraph(navController: NavController) {
    navigation<CatanCompanionNavGraph>(startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            DashboardScreenRoot(
                onNewGame = { navController.navigate(GameConfigRoute) },
                onResumeGame = { gameId -> navController.navigate(GameplayRoute(gameId)) },
                onGamesList = { navController.navigate(GamesListRoute) },
                onPlayersList = { navController.navigate(PlayersListRoute()) }
            )
        }
        composable<GameConfigRoute> { backStackEntry ->
            val viewModel = koinViewModel<GameConfigViewModel>(viewModelStoreOwner = backStackEntry)
            val selectionViewModel = koinViewModel<PlayersSelectionViewModel>(viewModelStoreOwner = backStackEntry)
            val pendingSelection by selectionViewModel.pendingSelection.collectAsState()
            LaunchedEffect(pendingSelection) {
                pendingSelection?.let { players ->
                    viewModel.onAction(GameConfigAction.PlayersSelected(players))
                    selectionViewModel.clearSelection()
                }
            }
            GameConfigScreenRoot(
                onNavigateBack = { navController.popBackStack() },
                onGameCreated = { gameId ->
                    navController.navigate(GameplayRoute(gameId)) {
                        popUpTo<DashboardRoute>()
                    }
                },
                onAddPlayer = { navController.navigate(PlayersListRoute(selectionMode = true)) },
                viewModel = viewModel
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
            val selectionViewModel = koinViewModel<PlayersSelectionViewModel>(
                viewModelStoreOwner = if (route.selectionMode) {
                    navController.getBackStackEntry(GameConfigRoute)
                } else {
                    backStackEntry
                }
            )
            PlayersListScreenRoot(
                isSelectionMode = route.selectionMode,
                onNavigateBack = { navController.popBackStack() },
                onPlayerClick = { playerId -> navController.navigate(PlayerDetailsRoute(playerId)) },
                onPlayersSelected = { players ->
                    selectionViewModel.setSelectedPlayers(players)
                    navController.popBackStack()
                }
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
