package org.example.project.catan_companion_feature.data.local.converter

import androidx.room.TypeConverter
import org.example.project.catan_companion_feature.domain.enums.EventDiceType

class EventDiceTypeConverter {
    @TypeConverter
    fun fromEventDiceType(type: EventDiceType?): String? = type?.name

    @TypeConverter
    fun toEventDiceType(value: String?): EventDiceType? =
        value?.let { EventDiceType.valueOf(it) }
}