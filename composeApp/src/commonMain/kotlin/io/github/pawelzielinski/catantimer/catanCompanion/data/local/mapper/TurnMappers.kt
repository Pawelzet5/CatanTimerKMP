package io.github.pawelzielinski.catantimer.catanCompanion.data.local.mapper

import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.TurnEntity
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn

fun TurnEntity.toDomain(playerName: String, secondaryPlayerName: String? = null): Turn = Turn(
    id = id,
    gameId = gameId,
    number = number,
    playerId = playerId,
    playerName = playerName,
    secondaryPlayerId = secondaryPlayerId,
    secondaryPlayerName = secondaryPlayerName,
    redDice = redDice,
    yellowDice = yellowDice,
    eventDice = eventDice,
    durationMillis = durationMillis
)

fun Turn.toEntity(): TurnEntity = TurnEntity(
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
