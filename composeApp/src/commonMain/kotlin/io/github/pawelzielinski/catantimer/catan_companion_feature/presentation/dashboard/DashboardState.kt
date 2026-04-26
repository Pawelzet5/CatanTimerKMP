package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.dashboard

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game

data class DashboardState(
    val resumableGame: Game? = null,
    val totalGames: Int = 0,
    val totalPlayers: Int = 0,
    val isLoading: Boolean = false
)
