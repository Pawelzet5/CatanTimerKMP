package io.github.pawelzielinski.catantimer.catanCompanion.data.local.converter

import androidx.room.TypeConverter
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType

class EventDiceTypeConverter {
    @TypeConverter
    fun fromEventDiceType(type: EventDiceType?): String? = type?.name

    @TypeConverter
    fun toEventDiceType(value: String?): EventDiceType? =
        value?.let { EventDiceType.valueOf(it) }
}