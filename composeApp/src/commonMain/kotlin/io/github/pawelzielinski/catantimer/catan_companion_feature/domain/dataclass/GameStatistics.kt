package io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType

data class GameStatistics(
    val totalTurns: Int,
    val averageTurnDurationMillis: Long,
    val diceDistribution: DiceDistribution,
    val eventDiceDistribution: Map<EventDiceType, Int>,
    val playerAverageTurnDurations: Map<Long, Long>  // playerId -> avgMillis
)
