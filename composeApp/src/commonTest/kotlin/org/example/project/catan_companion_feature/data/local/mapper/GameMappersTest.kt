package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameMappersTest {

    @Test
    fun `toDomain mapping, GameEntity with all fields, maps all fields`() {
        val players = listOf(
            GamePlayer(gameId = 1L, playerId = 1L, playerName = "Alice", orderIndex = 0)
        )
        val entity = GameEntity(
            id = 1L, turnDurationMillis = 180_000L,
            expansions = setOf(GameExpansion.CITIES_AND_KNIGHTS),
            specialTurnRuleEnabled = true,
            status = GameStatus.IN_PROGRESS,
            startedAt = 1_000L, finishedAt = 2_000L, winnerId = 5L
        )

        val domain = entity.toDomain(players)

        assertEquals(1L, domain.id)
        assertEquals(180_000L, domain.turnDurationMillis)
        assertEquals(setOf(GameExpansion.CITIES_AND_KNIGHTS), domain.expansions)
        assertTrue(domain.specialTurnRuleEnabled)
        assertEquals(GameStatus.IN_PROGRESS, domain.status)
        assertEquals(1_000L, domain.startedAt)
        assertEquals(2_000L, domain.finishedAt)
        assertEquals(5L, domain.winnerId)
        assertEquals(players, domain.players)
    }

    @Test
    fun `toDomain mapping, GameEntity with empty players list, maps correctly`() {
        val entity = GameEntity(
            id = 2L, turnDurationMillis = 60_000L,
            expansions = emptySet(), specialTurnRuleEnabled = false,
            status = GameStatus.COMPLETED, startedAt = 1_000L
        )

        val domain = entity.toDomain()

        assertTrue(domain.players.isEmpty())
        assertNull(domain.finishedAt)
        assertNull(domain.winnerId)
    }

    @Test
    fun `toEntity mapping, Game with players list, drops players list`() {
        val players = listOf(
            GamePlayer(gameId = 1L, playerId = 1L, playerName = "Alice", orderIndex = 0)
        )
        val domain = Game(
            id = 1L, turnDurationMillis = 60_000L,
            expansions = setOf(GameExpansion.SEAFARERS),
            specialTurnRuleEnabled = false,
            status = GameStatus.IN_PROGRESS,
            startedAt = 1_000L,
            players = players
        )

        val entity = domain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals(60_000L, entity.turnDurationMillis)
        assertEquals(setOf(GameExpansion.SEAFARERS), entity.expansions)
        assertEquals(GameStatus.IN_PROGRESS, entity.status)
        // players list is not stored in GameEntity — persisted separately via GamePlayerEntity
    }
}
