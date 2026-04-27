package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerdetails

sealed interface PlayerDetailsEvent {
    data object NavigateBack : PlayerDetailsEvent
}
