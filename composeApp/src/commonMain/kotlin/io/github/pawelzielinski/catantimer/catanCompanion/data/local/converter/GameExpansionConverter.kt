package io.github.pawelzielinski.catantimer.catanCompanion.data.local.converter

import androidx.room.TypeConverter
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion

class GameExpansionConverter {
    @TypeConverter
    fun fromExpansions(expansions: Set<GameExpansion>): String =
        expansions.joinToString(separator = ",") { it.name }

    @TypeConverter
    fun toExpansions(value: String): Set<GameExpansion> =
        if (value.isBlank()) emptySet()
        else value.split(",").mapTo(mutableSetOf()) { GameExpansion.valueOf(it) }
}