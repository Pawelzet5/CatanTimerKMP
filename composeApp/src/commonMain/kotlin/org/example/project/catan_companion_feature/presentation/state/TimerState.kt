package org.example.project.catan_companion_feature.presentation.state

data class TimerState(
    val remainingMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isForSecondaryPlayer: Boolean = false
)
