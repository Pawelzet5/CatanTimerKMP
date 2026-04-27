package io.github.pawelzielinski.catantimer.catanCompanion.presentation.service

// Why expect/actual: vibration API is entirely platform-specific.
// iOS uses UIFeedbackGenerator, Android uses Vibrator/VibrationEffect,
// Desktop has no equivalent (no-op).
expect object HapticService {
    fun vibrateOnce()
    fun vibrateOnceHeavy()
    fun vibrateMultiple()
}
