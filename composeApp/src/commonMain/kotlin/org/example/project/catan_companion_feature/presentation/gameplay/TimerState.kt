package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay

data class TimerState(
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isForSecondaryPlayer: Boolean = false
)
