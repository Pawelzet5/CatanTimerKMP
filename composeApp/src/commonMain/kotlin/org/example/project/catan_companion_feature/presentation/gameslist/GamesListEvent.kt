package org.example.project.catan_companion_feature.presentation.gameslist

sealed interface GamesListEvent {
    data object NavigateBack : GamesListEvent
    data class NavigateToGame(val gameId: Long) : GamesListEvent
}
