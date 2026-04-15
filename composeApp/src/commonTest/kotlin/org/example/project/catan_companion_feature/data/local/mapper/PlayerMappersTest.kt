package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
import org.example.project.catan_companion_feature.domain.dataclass.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerMappersTest {

    @Test
    fun `PlayerEntity toDomain maps all fields correctly`() {
        val entity = PlayerEntity(id = 5L, name = "Alice", isHidden = false)

        val domain = entity.toDomain(gamesPlayed = 3, gamesWon = 1)

        assertEquals(5L, domain.id)
        assertEquals("Alice", domain.name)
        assertFalse(domain.isHidden)
        assertEquals(3, domain.gamesPlayed)
        assertEquals(1, domain.gamesWon)
    }

    @Test
    fun `PlayerEntity toDomain with default gamesPlayed and gamesWon`() {
        val entity = PlayerEntity(id = 1L, name = "Bob", isHidden = true)

        val domain = entity.toDomain()

        assertEquals(0, domain.gamesPlayed)
        assertEquals(0, domain.gamesWon)
    }

    @Test
    fun `Player toEntity drops derived fields`() {
        val domain = Player(id = 7L, name = "Charlie", isHidden = true, gamesPlayed = 10, gamesWon = 5)

        val entity = domain.toEntity()

        assertEquals(7L, entity.id)
        assertEquals("Charlie", entity.name)
        assertTrue(entity.isHidden)
        // gamesPlayed and gamesWon are derived — not stored in PlayerEntity
    }
}
