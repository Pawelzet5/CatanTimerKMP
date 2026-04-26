package io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.mapper

import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.PlayerEntity
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerMappersTest {

    @Test
    fun `toDomain mapping, PlayerEntity with all fields, maps all fields correctly`() {
        val entity = PlayerEntity(id = 5L, name = "Alice", isHidden = false)

        val domain = entity.toDomain(gamesPlayed = 3, gamesWon = 1)

        assertEquals(5L, domain.id)
        assertEquals("Alice", domain.name)
        assertFalse(domain.isHidden)
        assertEquals(3, domain.gamesPlayed)
        assertEquals(1, domain.gamesWon)
    }

    @Test
    fun `toDomain mapping, PlayerEntity with default values, gamesPlayed and gamesWon default to zero`() {
        val entity = PlayerEntity(id = 1L, name = "Bob", isHidden = true)

        val domain = entity.toDomain()

        assertEquals(0, domain.gamesPlayed)
        assertEquals(0, domain.gamesWon)
    }

    @Test
    fun `toEntity mapping, Player with derived fields, drops gamesPlayed and gamesWon`() {
        val domain = Player(id = 7L, name = "Charlie", isHidden = true, gamesPlayed = 10, gamesWon = 5)

        val entity = domain.toEntity()

        assertEquals(7L, entity.id)
        assertEquals("Charlie", entity.name)
        assertTrue(entity.isHidden)
        // gamesPlayed and gamesWon are derived — not stored in PlayerEntity
    }
}
