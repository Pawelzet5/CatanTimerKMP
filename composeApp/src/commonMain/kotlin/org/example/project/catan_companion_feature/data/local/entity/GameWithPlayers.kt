package org.example.project.catan_companion_feature.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GameWithPlayers(
    @Embedded val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GamePlayerCrossRefEntity::class,
            parentColumn = "gameId",
            entityColumn = "playerId"
        )
    )
    val players: List<PlayerEntity>
    // Note: player order is NOT guaranteed by @Relation.
    // Use GameDao.getPlayersForGame(gameId) with ORDER BY playerIndex
    // whenever you need a list consistent with GameConfig.players.
)