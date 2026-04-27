package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.GameStatistics

data class GameSummaryState(
    val game: Game? = null,
    val statistics: GameStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
