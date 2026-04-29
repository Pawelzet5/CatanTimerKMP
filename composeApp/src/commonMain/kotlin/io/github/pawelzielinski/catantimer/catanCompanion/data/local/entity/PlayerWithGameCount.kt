package io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class PlayerWithGameCount(
    @Embedded val player: PlayerEntity,
    @ColumnInfo(name = "gamesPlayed") val gamesPlayed: Int
)
