package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig

sealed interface GameConfigEvent {
    data object NavigateBack : GameConfigEvent
    data class NavigateToGameplay(val gameId: Long) : GameConfigEvent
    data object NavigateToPlayerSelection : GameConfigEvent
}
