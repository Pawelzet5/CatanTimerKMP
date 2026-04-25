package io.github.pawelzielinski.catantimer.core.util

// Platform-specific date formatting is required because KMP has no common
// date/time API for locale-aware formatting of epoch timestamps.
expect fun formatEpochMillis(epochMillis: Long): String
expect fun formatEpochMillisToMonthYear(epochMillis: Long): String

// Platform-specific because java.lang.System is unavailable on Kotlin/Native.
expect fun currentTimeMillis(): Long
