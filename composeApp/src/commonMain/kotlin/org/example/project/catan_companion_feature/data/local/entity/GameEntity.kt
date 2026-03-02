package org.example.project.catan_companion_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.example.project.catan_companion_feature.domain.enums.GameStatus

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: String,           // TypeConverter: Set<GameExpansion> <-> CSV String
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus
)