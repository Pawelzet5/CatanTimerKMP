package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.example.project.catan_companion_feature.presentation.dashboard.DashboardScreen
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigScreen
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayScreen
import org.example.project.catan_companion_feature.presentation.gameslist.GamesListScreen
import org.example.project.catan_companion_feature.presentation.gamesummary.GameSummaryScreen
import org.example.project.catan_companion_feature.presentation.navigation.DashboardRoute
import org.example.project.catan_companion_feature.presentation.navigation.GameConfigRoute
import org.example.project.catan_companion_feature.presentation.navigation.GameSummaryRoute
import org.example.project.catan_companion_feature.presentation.navigation.GameplayRoute
import org.example.project.catan_companion_feature.presentation.navigation.GamesListRoute
import org.example.project.catan_companion_feature.presentation.navigation.PlayerDetailsRoute
import org.example.project.catan_companion_feature.presentation.navigation.PlayersListRoute
import org.example.project.catan_companion_feature.presentation.playerdetails.PlayerDetailsScreen
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListScreen
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
                DashboardScreen(
                    onNewGame = { navController.navigate(GameConfigRoute) },
                    onResumeGame = { gameId -> navController.navigate(GameplayRoute(gameId)) },
                    onGamesList = { navController.navigate(GamesListRoute) },
                    onPlayersList = { navController.navigate(PlayersListRoute()) }
                )
            }
            composable<GameConfigRoute> { backStackEntry ->
                val viewModel = koinViewModel<GameConfigViewModel>()

                val selectedPlayerId by backStackEntry.savedStateHandle
                    .getStateFlow<Long?>("selected_player_id", null)
                    .collectAsState()

                LaunchedEffect(selectedPlayerId) {
                    selectedPlayerId?.let { id ->
                        viewModel.onPlayerSelectedById(id)
                        backStackEntry.savedStateHandle.remove<Long>("selected_player_id")
                    }
                }

                GameConfigScreen(
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
                GameplayScreen(
                    gameId = route.gameId,
                    onNavigateToSummary = { gameId ->
                        navController.navigate(GameSummaryRoute(gameId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<PlayersListRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PlayersListRoute>()
                PlayersListScreen(
                    isSelectionMode = route.selectionMode,
                    onNavigateBack = { navController.popBackStack() },
                    onPlayerClick = { playerId -> navController.navigate(PlayerDetailsRoute(playerId)) },
                    onPlayerSelected = { player ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_player_id", player.id)
                        navController.popBackStack()
                    }
                )
            }
            composable<PlayerDetailsRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PlayerDetailsRoute>()
                PlayerDetailsScreen(
                    playerId = route.playerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<GamesListRoute> {
                GamesListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onGameClick = { gameId -> navController.navigate(GameSummaryRoute(gameId)) }
                )
            }
            composable<GameSummaryRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<GameSummaryRoute>()
                GameSummaryScreen(
                    gameId = route.gameId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
