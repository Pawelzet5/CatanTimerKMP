package io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard

sealed interface DashboardAction {
    data object NewGameClick : DashboardAction
    data class ResumeGameClick(val gameId: Long) : DashboardAction
    data object GamesListClick : DashboardAction
    data object PlayersListClick : DashboardAction
}
