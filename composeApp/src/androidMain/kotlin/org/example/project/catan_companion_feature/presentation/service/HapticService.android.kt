package org.example.project.catan_companion_feature.presentation.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

actual class HapticService(private val context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    actual fun vibrateOnce() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            vibrator?.vibrate(100)
        }
    }

    actual fun vibrateOnceHeavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(500, 255))
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            vibrator?.vibrate(500)
        }
    }

    actual fun vibrateMultiple() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 80, 200, 80, 400),
                    intArrayOf(0, 150, 0, 200, 0, 255),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            vibrator?.vibrate(longArrayOf(0, 100, 80, 200, 80, 400), -1)
        }
    }
}
