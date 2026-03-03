package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.TurnEntity
import org.example.project.catan_companion_feature.domain.dataclass.Turn

fun TurnEntity.toDomain(): Turn = Turn(
    id = id,
    number = number,
    playerId = playerId,
    secondaryPlayerId = secondaryPlayerId,
    redDice = redDice,
    yellowDice = yellowDice,
    eventDice = eventDice,
    durationMillis = durationMillis
)

fun Turn.toEntity(gameId: Long): TurnEntity = TurnEntity(
    id = id,
    gameId = gameId,
    number = number,
    playerId = playerId,
    secondaryPlayerId = secondaryPlayerId,
    redDice = redDice,
    yellowDice = yellowDice,
    eventDice = eventDice,
    durationMillis = durationMillis
)