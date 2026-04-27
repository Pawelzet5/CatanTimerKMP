package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary

sealed interface GameSummaryEvent {
    data object NavigateBack : GameSummaryEvent
    data object NavigateHome : GameSummaryEvent
}
