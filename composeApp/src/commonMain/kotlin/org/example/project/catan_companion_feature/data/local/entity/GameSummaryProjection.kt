package org.example.project.catan_companion_feature.data.local.entity

import androidx.room.ColumnInfo
import org.example.project.catan_companion_feature.domain.enums.GameStatus

data class GameSummaryProjection(
    val id: Long,
    val status: GameStatus,
    @ColumnInfo(name = "playerCount") val playerCount: Int,
    @ColumnInfo(name = "turnCount") val turnCount: Int,
    val startedAt: Long,
    val finishedAt: Long?
)