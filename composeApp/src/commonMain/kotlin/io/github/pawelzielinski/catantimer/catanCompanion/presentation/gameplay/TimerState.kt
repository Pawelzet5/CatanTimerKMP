package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay

data class TimerState(
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isForSecondaryPlayer: Boolean = false
)
