package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GameSummaryProjection
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.domain.dataclass.GameSummary
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus

fun GameEntity.toDomain(players: List<Player>): Game = Game(
    id = id,
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    players = players.mapIndexed { index, player ->
        GamePlayer(
            gameId = id,
            playerId = player.id,
            playerName = player.name,
            orderIndex = index
        )
    }
)

fun GameSummaryProjection.toDomain(): GameSummary = GameSummary(
    id = id,
    status = status,
    playerCount = playerCount,
    turnCount = turnCount,
    startedAt = startedAt,
    finishedAt = finishedAt
)

fun toGameEntity(
    turnDurationMillis: Long,
    expansions: Set<GameExpansion>,
    specialTurnRuleEnabled: Boolean,
    startedAt: Long,
    status: GameStatus = GameStatus.IN_PROGRESS
): GameEntity = GameEntity(
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt
)
