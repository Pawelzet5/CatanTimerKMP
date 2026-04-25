package org.example.project.catan_companion_feature.presentation.gamesummary

import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameStatistics

data class GameSummaryState(
    val game: Game? = null,
    val statistics: GameStatistics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
