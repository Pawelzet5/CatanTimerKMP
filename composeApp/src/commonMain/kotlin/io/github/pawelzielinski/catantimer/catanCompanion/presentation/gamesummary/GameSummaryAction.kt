package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary

sealed interface GameSummaryAction {
    data object BackClick : GameSummaryAction
    data object HomeClick : GameSummaryAction
}
