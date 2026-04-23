package org.example.project.catan_companion_feature.presentation.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

actual object HapticService {
    private var vibrator: Vibrator? = null

    // Called from the Android Koin platform module to inject Context without a constructor parameter.
    fun initialize(context: Context) {
        vibrator = context.applicationContext.getSystemService(Vibrator::class.java)
    }

    actual fun vibrateOnce() {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            v.vibrate(100)
        }
    }

    actual fun vibrateOnceHeavy() {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, 255))
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            v.vibrate(500)
        }
    }

    actual fun vibrateMultiple() {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 80, 200, 80, 400),
                    intArrayOf(0, 150, 0, 200, 0, 255),
                    -1
                )
            )
        } else {
            @Suppress("DEPRECATION") // Required for API < 26 support
            v.vibrate(longArrayOf(0, 100, 80, 200, 80, 400), -1)
        }
    }
}
