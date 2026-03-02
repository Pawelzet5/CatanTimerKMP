package org.example.project.catan_companion_feature.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "game_player_cross_ref",
    primaryKeys = ["gameId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            // prevents deletion of a player who participated in a game
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("gameId"),
        Index("playerId")
    ]
)
data class GamePlayerCrossRefEntity(
    val gameId: Long,
    val playerId: Long,
    // player's position within this specific game; ORDER BY playerIndex restores GameConfig.players order
    val playerIndex: Int
)