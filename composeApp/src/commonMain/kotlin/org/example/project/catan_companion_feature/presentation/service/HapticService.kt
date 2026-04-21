package org.example.project.catan_companion_feature.presentation.service

// Why expect/actual: vibration API is entirely platform-specific.
// iOS uses UIFeedbackGenerator, Android uses Vibrator/VibrationEffect,
// Desktop has no equivalent (no-op).
expect class HapticService {
    fun vibrateTimerEnd()
}
