package io.github.pawelzielinski.catantimer.core.util

import kotlin.time.Instant

fun formatDuration(startMillis: Long, endMillis: Long): String {
    val start = Instant.fromEpochMilliseconds(startMillis)
    val end = Instant.fromEpochMilliseconds(endMillis.coerceAtLeast(startMillis))
    val duration = end - start
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
}
