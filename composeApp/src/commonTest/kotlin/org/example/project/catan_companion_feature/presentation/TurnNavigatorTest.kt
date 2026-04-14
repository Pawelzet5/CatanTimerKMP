package org.example.project.catan_companion_feature.presentation

import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.presentation.util.TurnNavigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TurnNavigatorTest {

    private val turns = listOf(
        turn(id = 1, number = 0),
        turn(id = 2, number = 1),
        turn(id = 3, number = 2)
    )

    @Test
    fun `default construction selects last turn`() {
        val nav = TurnNavigator(turns)
        assertEquals(turns.last(), nav.selectedTurn)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `selectPrevious moves to previous turn`() {
        val nav = TurnNavigator(turns).selectPrevious()
        assertEquals(turns[1], nav.selectedTurn)
        assertFalse(nav.isViewingLatest)
    }

    @Test
    fun `selectPrevious at first turn is no-op`() {
        val nav = TurnNavigator(turns, selectedIndex = 0).selectPrevious()
        assertEquals(turns[0], nav.selectedTurn)
    }

    @Test
    fun `selectNext at last turn is no-op`() {
        val nav = TurnNavigator(turns).selectNext()
        assertEquals(turns.last(), nav.selectedTurn)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `selectLatest from middle returns to last`() {
        val nav = TurnNavigator(turns, selectedIndex = 0).selectLatest()
        assertEquals(turns.last(), nav.selectedTurn)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `single turn list — all navigation is no-op`() {
        val nav = TurnNavigator(listOf(turn(id = 1, number = 0)))
        assertFalse(nav.hasPrevious)
        assertFalse(nav.hasNext)
        assertTrue(nav.isViewingLatest)
    }

    @Test
    fun `empty list returns null selectedTurn`() {
        val nav = TurnNavigator(emptyList())
        assertNull(nav.selectedTurn)
    }

    private fun turn(id: Long, number: Int) = Turn(
        id = id, gameId = 1L, number = number,
        playerId = 1L, playerName = "Test"
    )
}
