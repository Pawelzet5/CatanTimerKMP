package io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass

import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType

data class Turn(
    val id: Long = 0L,
    val gameId: Long,
    val number: Int,
    val playerId: Long,
    val playerName: String,
    val secondaryPlayerId: Long? = null,
    val secondaryPlayerName: String? = null,
    val redDice: Int? = null,
    val yellowDice: Int? = null,
    val eventDice: EventDiceType? = null,
    val durationMillis: Long = 0L
) {
    val diceSum: Int?
        get() = if (redDice != null && yellowDice != null) redDice + yellowDice else null

    val isRobberRoll: Boolean
        get() = diceSum == 7
}
