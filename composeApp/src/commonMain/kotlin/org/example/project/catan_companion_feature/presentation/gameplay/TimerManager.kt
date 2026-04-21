package org.example.project.catan_companion_feature.presentation.gameplay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TimerManager {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun start(scope: CoroutineScope, fromMillis: Long) {
        timerJob?.cancel()
        _state.update { it.copy(remainingMillis = fromMillis, isRunning = true) }
        timerJob = scope.launch {
            while (_state.value.remainingMillis > 0) {
                delay(100L)
                _state.update { current ->
                    current.copy(remainingMillis = (current.remainingMillis - 100L).coerceAtLeast(0L))
                }
            }
            _state.update { it.copy(isRunning = false) }
            // Timer reached zero — ViewModel observes state and triggers HapticService
        }
    }

    fun stop(): Long {
        timerJob?.cancel()
        timerJob = null
        val elapsed = _state.value.remainingMillis
        _state.update { it.copy(isRunning = false) }
        return elapsed
    }

    fun addTime(millis: Long) {
        _state.update { it.copy(remainingMillis = it.remainingMillis + millis) }
    }

    fun reset(durationMillis: Long) {
        timerJob?.cancel()
        timerJob = null
        _state.update { TimerState(remainingMillis = durationMillis, isRunning = false) }
    }
}
