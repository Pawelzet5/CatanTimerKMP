package io.github.pawelzielinski.catantimer.catanCompanion.data.local.mapper

import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GameEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GamePlayerEntity
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.GamePlayer

fun GameEntity.toDomain(players: List<GamePlayer> = emptyList()): Game = Game(
    id = id,
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    winnerId = winnerId,
    players = players
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    winnerId = winnerId
)

fun GamePlayerEntity.toDomain(playerName: String): GamePlayer = GamePlayer(
    gameId = gameId,
    playerId = playerId,
    playerName = playerName,
    orderIndex = orderIndex
)
