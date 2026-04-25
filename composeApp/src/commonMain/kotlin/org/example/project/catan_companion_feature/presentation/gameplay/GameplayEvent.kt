package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay

sealed interface GameplayEvent {
    data class NavigateToWinnerSelection(val gameId: Long) : GameplayEvent
    data class NavigateToGameSummary(val gameId: Long) : GameplayEvent
    data object NavigateToGameConfig : GameplayEvent
}
