package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gamesummary

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.GameStatistics

data class GameSummaryState(
    val game: Game? = null,
    val statistics: GameStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
