package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay

sealed interface GameplayEvent {
    data class NavigateToWinnerSelection(val gameId: Long) : GameplayEvent
    data class NavigateToGameSummary(val gameId: Long) : GameplayEvent
    data object NavigateToGameConfig : GameplayEvent
}
