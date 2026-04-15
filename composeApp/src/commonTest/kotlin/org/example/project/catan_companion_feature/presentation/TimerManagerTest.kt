package org.example.project.catan_companion_feature.presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.presentation.state.TimerState
import org.example.project.catan_companion_feature.presentation.timer.TimerManager
import kotlin.test.Test
import kotlin.test.assertEquals

class TimerManagerTest {

    @Test
    fun `initial state has zero remaining and is not running`() = runTest {
        val timer = TimerManager(this)
        assertEquals(TimerState(remainingMillis = 0L, isRunning = false), timer.state.value)
    }

    @Test
    fun `start sets isRunning to true`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        assertEquals(TimerState(remainingMillis = 60_000L, isRunning = true), timer.state.value)
    }

    @Test
    fun `stop cancels timer and returns remaining millis`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        val remaining = timer.stop()
        assertEquals(60_000L, remaining)
        assertEquals(TimerState(remainingMillis = 60_000L, isRunning = false), timer.state.value)
    }

    @Test
    fun `addTime increases remaining millis`() = runTest {
        val timer = TimerManager(this)
        timer.reset(30_000L)
        timer.addTime(10_000L)
        assertEquals(TimerState(remainingMillis = 40_000L, isRunning = false), timer.state.value)
    }

    @Test
    fun `reset sets remaining to given duration and stops timer`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        timer.reset(120_000L)
        assertEquals(TimerState(remainingMillis = 120_000L, isRunning = false), timer.state.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `timer reaches zero and sets isRunning to false`() = runTest {
        val timer = TimerManager(this)
        timer.start(200L)
        advanceTimeBy(500L)
        assertEquals(TimerState(remainingMillis = 0L, isRunning = false), timer.state.value)
    }
}
