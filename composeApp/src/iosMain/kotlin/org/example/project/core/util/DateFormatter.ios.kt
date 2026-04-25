package org.example.project.core.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.time.Instant

actual fun formatEpochMillis(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val date = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
    val formatter = NSDateFormatter()
    formatter.dateFormat = GAME_DATE_PATTERN
    return formatter.stringFromDate(date)
}

actual fun formatEpochMillisToMonthYear(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val date = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
    val formatter = NSDateFormatter()
    formatter.dateFormat = MONTH_YEAR_PATTERN
    return formatter.stringFromDate(date)
}

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
