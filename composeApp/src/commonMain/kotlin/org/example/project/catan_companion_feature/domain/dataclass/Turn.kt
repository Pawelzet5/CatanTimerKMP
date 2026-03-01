package org.example.project.catan_companion_feature.domain.dataclass

import org.example.project.catan_companion_feature.domain.enums.EventDiceType


data class Turn(
    val id: Long = 0L,
    val number: Int,
    val playerId: Long,
    val secondaryPlayerId: Long? = null,
    val redDice: Int? = null,
    val yellowDice: Int? = null,
    val eventDice: EventDiceType? = null,
    val durationMillis: Long = 0L
)