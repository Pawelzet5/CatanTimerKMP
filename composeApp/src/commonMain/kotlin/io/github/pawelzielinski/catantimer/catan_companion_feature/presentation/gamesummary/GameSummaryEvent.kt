package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gamesummary

sealed interface GameSummaryEvent {
    data object NavigateBack : GameSummaryEvent
    data object NavigateHome : GameSummaryEvent
}
