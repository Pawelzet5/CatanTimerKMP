package io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameStatus

data class GameSummary(
    val id: Long,
    val status: GameStatus,
    val playerCount: Int,
    val turnCount: Int,
    val startedAt: Long,
    val finishedAt: Long? = null
)
