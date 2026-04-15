package org.example.project.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatEpochMillis(epochMillis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(epochMillis))
