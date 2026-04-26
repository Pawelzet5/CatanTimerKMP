package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gamesummary

sealed interface GameSummaryAction {
    data object BackClick : GameSummaryAction
    data object HomeClick : GameSummaryAction
}
