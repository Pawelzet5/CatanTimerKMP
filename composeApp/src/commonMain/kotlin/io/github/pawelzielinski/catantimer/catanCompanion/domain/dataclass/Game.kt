package io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass

import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameStatus

data class Game(
    val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val winnerId: Long? = null,
    val players: List<GamePlayer> = emptyList()
)
