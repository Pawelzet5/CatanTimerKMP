package io.github.pawelzielinski.catantimer.catanCompanion.data.local.converter

import androidx.room.TypeConverter
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameStatus

class GameStatusConverter {
    @TypeConverter
    fun fromGameStatus(status: GameStatus): String = status.name

    @TypeConverter
    fun toGameStatus(value: String): GameStatus = GameStatus.valueOf(value)
}