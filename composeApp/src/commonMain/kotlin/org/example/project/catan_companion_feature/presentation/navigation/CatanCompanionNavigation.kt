package org.example.project.catan_companion_feature.presentation.navigation

sealed class CatanCompanionRoute(val route: String) {
    object Dashboard : CatanCompanionRoute("dashboard")
    object GameConfig : CatanCompanionRoute("game_config")
    object Gameplay : CatanCompanionRoute("gameplay/{gameId}") {
        fun createRoute(gameId: Long) = "gameplay/$gameId"
    }
    object PlayersList : CatanCompanionRoute("players_list?selectionMode={selectionMode}") {
        fun createRoute(selectionMode: Boolean = false) = "players_list?selectionMode=$selectionMode"
    }
    object PlayerDetails : CatanCompanionRoute("player_details/{playerId}") {
        fun createRoute(playerId: Long) = "player_details/$playerId"
    }
    object GamesList : CatanCompanionRoute("games_list")
    object GameSummary : CatanCompanionRoute("game_summary/{gameId}") {
        fun createRoute(gameId: Long) = "game_summary/$gameId"
    }
    object WinnerSelection : CatanCompanionRoute("winner_selection/{gameId}") {
        fun createRoute(gameId: Long) = "winner_selection/$gameId"
    }
}
