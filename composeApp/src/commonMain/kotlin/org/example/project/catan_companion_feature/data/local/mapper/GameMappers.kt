package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerCrossRefEntity
import org.example.project.catan_companion_feature.data.local.entity.GameWithPlayers
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameStatus

fun GameWithPlayers.toDomain(): Game {
    // Players are sorted by ORDER BY playerIndex in the DAO query.
    // @Relation does not guarantee order – use getPlayersForGame instead
    // when order is critical (see GameRepositoryImpl).
    val playerList = players.map { it.toDomain() }
    val config = GameConfig(
        turnDurationMillis = game.turnDurationMillis,
        expansions = game.expansions,
        specialTurnRuleEnabled = game.specialTurnRuleEnabled,
        players = playerList
    )
    return Game(id = game.id, config = config, status = game.status)
}

fun GameConfig.toEntity(): GameEntity = GameEntity(
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = GameStatus.ACTIVE
)

fun buildGamePlayerCrossRefs(gameId: Long, players: List<Player>): List<GamePlayerCrossRefEntity> =
    players.mapIndexed { index, player ->
        GamePlayerCrossRefEntity(
            gameId = gameId,
            playerId = player.id,
            playerIndex = index
        )
    }