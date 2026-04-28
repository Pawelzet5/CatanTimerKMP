package io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game

data class DashboardState(
    val resumableGame: Game? = null,
    val totalGames: Int = 0,
    val totalPlayers: Int = 0,
    val isLoading: Boolean = false
)
