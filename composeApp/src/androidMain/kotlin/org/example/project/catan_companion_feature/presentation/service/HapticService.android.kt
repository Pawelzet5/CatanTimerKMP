package org.example.project.catan_companion_feature.presentation.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

actual class HapticService(private val context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    actual fun vibrateTimerEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100, 50, 100), -1
                )
            )
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            vibrator?.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
        }
    }
}
