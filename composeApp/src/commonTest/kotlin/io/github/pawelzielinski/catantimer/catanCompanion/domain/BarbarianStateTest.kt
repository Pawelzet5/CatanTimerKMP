package io.github.pawelzielinski.catantimer.catanCompanion.domain

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.toBarbarianState
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BarbarianStateTest {

    @Test
    fun `Barbarian state, zero barbarian rolls, position 0 no raids`() {
        val turns = listOf(turnWithEvent(EventDiceType.TRADE), turnWithEvent(EventDiceType.POLITICS))
        val state = turns.toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(0, state.raidsCompleted)
        assertFalse(state.hasFirstRaidOccurred)
    }

    @Test
    fun `Barbarian state, 7 barbarian rolls, position 7 no raids`() {
        val turns = barbarianTurns(7)
        val state = turns.toBarbarianState()
        assertEquals(7, state.position)
        assertEquals(0, state.raidsCompleted)
        assertFalse(state.hasFirstRaidOccurred)
    }

    @Test
    fun `Barbarian state, 8 barbarian rolls, resets to position 0 one raid completed`() {
        val turns = barbarianTurns(8)
        val state = turns.toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(1, state.raidsCompleted)
        assertTrue(state.hasFirstRaidOccurred)
    }

    @Test
    fun `Barbarian state, 15 barbarian rolls, position 7 one raid completed`() {
        val turns = barbarianTurns(15)
        val state = turns.toBarbarianState()
        assertEquals(7, state.position)
        assertEquals(1, state.raidsCompleted)
        assertTrue(state.hasFirstRaidOccurred)
    }

    @Test
    fun `Barbarian state, 16 barbarian rolls, resets to position 0 two raids completed`() {
        val turns = barbarianTurns(16)
        val state = turns.toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(2, state.raidsCompleted)
        assertTrue(state.hasFirstRaidOccurred)
    }

    @Test
    fun `Barbarian state, empty turn list, returns default state`() {
        val state = emptyList<Turn>().toBarbarianState()
        assertEquals(0, state.position)
        assertEquals(0, state.raidsCompleted)
        assertFalse(state.hasFirstRaidOccurred)
    }

    private fun barbarianTurns(count: Int) =
        (1..count).map { turnWithEvent(EventDiceType.BARBARIANS) }

    private fun turnWithEvent(event: EventDiceType?) = Turn(
        id = 0L, gameId = 1L, number = 0,
        playerId = 1L, playerName = "Test",
        eventDice = event
    )
}
