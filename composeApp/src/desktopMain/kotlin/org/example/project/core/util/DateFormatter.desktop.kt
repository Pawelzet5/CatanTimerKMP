package org.example.project.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

actual fun formatEpochMillis(epochMillis: Long): String {
    val localDateTime = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime()
    return DateTimeFormatter.ofPattern(GAME_DATE_PATTERN, Locale.getDefault())
        .format(localDateTime)
}

actual fun formatEpochMillisToMonthYear(epochMillis: Long): String {
    val localDateTime = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime()
    return DateTimeFormatter.ofPattern(MONTH_YEAR_PATTERN, Locale.getDefault())
        .format(localDateTime)
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
