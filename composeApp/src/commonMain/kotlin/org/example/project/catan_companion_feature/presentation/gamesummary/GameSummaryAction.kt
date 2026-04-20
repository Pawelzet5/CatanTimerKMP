package org.example.project.catan_companion_feature.presentation.gamesummary

sealed interface GameSummaryAction {
    data object BackClick : GameSummaryAction
}
