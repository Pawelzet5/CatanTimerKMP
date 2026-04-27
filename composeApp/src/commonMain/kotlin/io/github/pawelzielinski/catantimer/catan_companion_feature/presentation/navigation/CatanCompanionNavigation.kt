package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable data object CatanCompanionNavGraph
@Serializable data object DashboardRoute
@Serializable data object GameConfigRoute
@Serializable data class GameplayRoute(val gameId: Long)
@Serializable data class PlayersListRoute(val selectionMode: Boolean = false)
@Serializable data class PlayerDetailsRoute(val playerId: Long)
@Serializable data object GamesListRoute
@Serializable data class GameSummaryRoute(val gameId: Long)
@Serializable data class WinnerSelectionRoute(val gameId: Long)
