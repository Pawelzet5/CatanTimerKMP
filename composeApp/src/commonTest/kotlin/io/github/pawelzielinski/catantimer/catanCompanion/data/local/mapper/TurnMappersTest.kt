package io.github.pawelzielinski.catantimer.catanCompanion.data.local.mapper

import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.TurnEntity
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TurnMappersTest {

    @Test
    fun `toDomain mapping, TurnEntity with all fields, maps all fields including playerName`() {
        val entity = TurnEntity(
            id = 1L, gameId = 2L, number = 3, playerId = 4L,
            secondaryPlayerId = 5L, redDice = 3, yellowDice = 4,
            eventDice = EventDiceType.BARBARIANS, durationMillis = 30_000L
        )

        val domain = entity.toDomain(playerName = "Alice", secondaryPlayerName = "Bob")

        assertEquals(1L, domain.id)
        assertEquals(2L, domain.gameId)
        assertEquals(3, domain.number)
        assertEquals(4L, domain.playerId)
        assertEquals("Alice", domain.playerName)
        assertEquals(5L, domain.secondaryPlayerId)
        assertEquals("Bob", domain.secondaryPlayerName)
        assertEquals(3, domain.redDice)
        assertEquals(4, domain.yellowDice)
        assertEquals(EventDiceType.BARBARIANS, domain.eventDice)
        assertEquals(30_000L, domain.durationMillis)
    }

    @Test
    fun `toDomain mapping, TurnEntity with null secondaryPlayerId, maps correctly`() {
        val entity = TurnEntity(
            id = 1L, gameId = 2L, number = 0, playerId = 1L,
            secondaryPlayerId = null
        )

        val domain = entity.toDomain(playerName = "Alice")

        assertNull(domain.secondaryPlayerId)
        assertNull(domain.secondaryPlayerName)
    }

    @Test
    fun `toEntity mapping, Turn with player names, drops playerName and secondaryPlayerName`() {
        val domain = Turn(
            id = 1L, gameId = 2L, number = 0, playerId = 1L,
            playerName = "Alice", secondaryPlayerName = "Bob",
            secondaryPlayerId = 2L, redDice = 3, yellowDice = 4,
            durationMillis = 60_000L
        )

        val entity = domain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals(2L, entity.gameId)
        assertEquals(0, entity.number)
        assertEquals(1L, entity.playerId)
        assertEquals(2L, entity.secondaryPlayerId)
        assertEquals(3, entity.redDice)
        assertEquals(4, entity.yellowDice)
        assertEquals(60_000L, entity.durationMillis)
        // playerName and secondaryPlayerName are not stored in TurnEntity
    }
}
