package org.example.project.catan_companion_feature.presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.presentation.timer.TimerManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimerManagerTest {

    @Test
    fun `Timer state, on creation, has zero remaining and is not running`() = runTest {
        val timer = TimerManager(this)
        assertEquals(0L, timer.state.value.remainingMillis)
        assertFalse(timer.state.value.isRunning)
    }

    @Test
    fun `Timer start, timer idle, sets isRunning to true`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        assertTrue(timer.state.value.isRunning)
        assertTrue(timer.state.value.remainingMillis <= 60_000L)
    }

    @Test
    fun `Timer stop, timer running, cancels timer and returns remaining millis`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        val remaining = timer.stop()
        assertFalse(timer.state.value.isRunning)
        assertTrue(remaining >= 0L)
    }

    @Test
    fun `addTime, timer has remaining time, increases remaining millis`() = runTest {
        val timer = TimerManager(this)
        timer.reset(30_000L)
        timer.addTime(10_000L)
        assertEquals(40_000L, timer.state.value.remainingMillis)
    }

    @Test
    fun `Timer reset, timer running, sets remaining to given duration and stops`() = runTest {
        val timer = TimerManager(this)
        timer.start(60_000L)
        timer.reset(120_000L)
        assertEquals(120_000L, timer.state.value.remainingMillis)
        assertFalse(timer.state.value.isRunning)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Timer expiry, duration elapsed, sets isRunning to false`() = runTest {
        val timer = TimerManager(this)
        timer.start(200L)
        advanceTimeBy(500L)
        assertEquals(0L, timer.state.value.remainingMillis)
        assertFalse(timer.state.value.isRunning)
    }
}
