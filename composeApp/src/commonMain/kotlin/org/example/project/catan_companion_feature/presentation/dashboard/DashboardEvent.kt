package org.example.project.catan_companion_feature.presentation.dashboard

sealed interface DashboardEvent {
    data object NavigateToNewGame : DashboardEvent
    data class NavigateToResumeGame(val gameId: Long) : DashboardEvent
    data object NavigateToGamesList : DashboardEvent
    data object NavigateToPlayersList : DashboardEvent
}
