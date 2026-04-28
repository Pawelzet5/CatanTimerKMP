package io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameStatus

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    val specialTurnRuleEnabled: Boolean,
    val status: GameStatus,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val winnerId: Long? = null
)