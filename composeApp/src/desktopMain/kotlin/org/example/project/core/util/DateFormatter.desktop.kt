package org.example.project.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatEpochMillis(epochMillis: Long): String =
    SimpleDateFormat(GAME_DATE_PATTERN, Locale.getDefault()).format(Date(epochMillis))
