package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameconfig

sealed interface GameConfigEvent {
    data object NavigateBack : GameConfigEvent
    data class NavigateToGameplay(val gameId: Long) : GameConfigEvent
    data object NavigateToPlayerSelection : GameConfigEvent
}
