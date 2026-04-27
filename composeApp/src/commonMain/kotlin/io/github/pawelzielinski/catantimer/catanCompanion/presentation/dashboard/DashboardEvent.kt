package io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard

sealed interface DashboardEvent {
    data object NavigateToNewGame : DashboardEvent
    data class NavigateToResumeGame(val gameId: Long) : DashboardEvent
    data object NavigateToGamesList : DashboardEvent
    data object NavigateToPlayersList : DashboardEvent
}
