package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameslist

sealed interface GamesListEvent {
    data object NavigateBack : GamesListEvent
    data class NavigateToGameplay(val gameId: Long) : GamesListEvent
    data class NavigateToGameSummary(val gameId: Long) : GamesListEvent
}
