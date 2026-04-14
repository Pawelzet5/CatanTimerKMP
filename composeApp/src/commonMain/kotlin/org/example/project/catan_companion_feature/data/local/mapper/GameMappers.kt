package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerCrossRefEntity
import org.example.project.catan_companion_feature.data.local.entity.GameSummaryProjection
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.catan_companion_feature.domain.dataclass.GameSummary
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameStatus

fun GameEntity.toDomain(players: List<Player>): Game {
    val config = GameConfig(
        turnDurationMillis = turnDurationMillis,
        expansions = expansions,
        specialTurnRuleEnabled = specialTurnRuleEnabled,
        players = players
    )
    return Game(
        id = id,
        config = config,
        status = status,
        startedAt = startedAt,
        finishedAt = finishedAt
    )
}

fun GameSummaryProjection.toDomain(): GameSummary = GameSummary(
    id = id,
    status = status,
    playerCount = playerCount,
    turnCount = turnCount,
    startedAt = startedAt,
    finishedAt = finishedAt
)

fun GameConfig.toEntity(
    startedAt: Long,
    status: GameStatus = GameStatus.IN_PROGRESS
): GameEntity = GameEntity(
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt
)

fun buildGamePlayerCrossRefs(gameId: Long, players: List<Player>): List<GamePlayerCrossRefEntity> =
    players.mapIndexed { index, player ->
        GamePlayerCrossRefEntity(
            gameId = gameId,
            playerId = player.id,
            playerIndex = index
        )
    }