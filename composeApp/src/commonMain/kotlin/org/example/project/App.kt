package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigAction
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigViewModel
import org.example.project.catan_companion_feature.presentation.gameconfig.PlayersSelectionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.example.project.catan_companion_feature.presentation.dashboard.DashboardScreenRoot
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigScreenRoot
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayScreenRoot
import org.example.project.catan_companion_feature.presentation.gameslist.GamesListScreenRoot
import org.example.project.catan_companion_feature.presentation.gamesummary.GameSummaryScreenRoot
import org.example.project.catan_companion_feature.presentation.winnerselection.WinnerSelectionScreenRoot
import org.example.project.catan_companion_feature.presentation.navigation.DashboardRoute
import org.example.project.catan_companion_feature.presentation.navigation.GameConfigRoute
import org.example.project.catan_companion_feature.presentation.navigation.GameSummaryRoute
import org.example.project.catan_companion_feature.presentation.navigation.GameplayRoute
import org.example.project.catan_companion_feature.presentation.navigation.GamesListRoute
import org.example.project.catan_companion_feature.presentation.navigation.PlayerDetailsRoute
import org.example.project.catan_companion_feature.presentation.navigation.PlayersListRoute
import org.example.project.catan_companion_feature.presentation.navigation.WinnerSelectionRoute
import org.example.project.catan_companion_feature.presentation.playerdetails.PlayerDetailsScreenRoot
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListScreenRoot
import org.example.project.core.designsystem.CatanTimerTheme

@Composable
fun App() {
    CatanTimerTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = DashboardRoute
        ) {
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
                val selectionViewModel = koinViewModel<PlayersSelectionViewModel>(
                    viewModelStoreOwner = backStackEntry
                )

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
                val route = backStackEntry.toRoute<GameplayRoute>()
                GameplayScreenRoot(
                    gameId = route.gameId,
                    onNavigateToSummary = { gameId ->
                        navController.navigate(GameSummaryRoute(gameId))
                    },
                    onNavigateToWinnerSelection = { gameId ->
                        navController.navigate(WinnerSelectionRoute(gameId))
                    },
                    onNavigateToGameConfig = {
                        navController.navigate(GameConfigRoute)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<PlayersListRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PlayersListRoute>()

                // Navigation contract: PlayersListRoute(selectionMode = true) is only navigated
                // to from GameConfigRoute, so its back stack entry is always present in that case.
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
                val route = backStackEntry.toRoute<PlayerDetailsRoute>()
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
                val route = backStackEntry.toRoute<WinnerSelectionRoute>()
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
                val route = backStackEntry.toRoute<GameSummaryRoute>()
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
}
