package org.example.project.catan_companion_feature.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.example.project.catan_companion_feature.domain.enums.EventDiceType

@Entity(
    tableName = "turns",
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
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["secondaryPlayerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("gameId"),
        Index("playerId"),
        Index("secondaryPlayerId")
    ]
)
data class TurnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val gameId: Long,
    val number: Int,
    val playerId: Long,
    val secondaryPlayerId: Long?,   // null when specialTurnRuleEnabled = false
    val redDice: Int?,
    val yellowDice: Int?,
    val eventDice: EventDiceType?,  // null when CITIES_AND_KNIGHTS disabled
    val durationMillis: Long
)