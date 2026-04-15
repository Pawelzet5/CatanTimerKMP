package org.example.project.catan_companion_feature.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakePlayerDao
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
import org.example.project.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlayerRepositoryImplTest {

    private val fakePlayerDao = FakePlayerDao()
    private val repository = PlayerRepositoryImpl(fakePlayerDao)

    @Test
    fun `getAllPlayers, players exist in database, emits Flow with list`() = runTest {
        fakePlayerDao.addPlayers(
            PlayerEntity(id = 1L, name = "Alice"),
            PlayerEntity(id = 2L, name = "Bob")
        )

        repository.getAllPlayers().test {
            val players = awaitItem()
            assertEquals(2, players.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `canDeletePlayer, player has games, returns false`() = runTest {
        fakePlayerDao.setGameCount(playerId = 1L, count = 3)

        val canDelete = repository.canDeletePlayer(id = 1L)

        assertFalse(canDelete)
    }

    @Test
    fun `canDeletePlayer, player has no games, returns true`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 1L, name = "Alice"))

        val canDelete = repository.canDeletePlayer(id = 1L)

        assertTrue(canDelete)
    }

    @Test
    fun `hidePlayer, player exists, sets isHidden to true`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 1L, name = "Alice", isHidden = false))

        val result = repository.hidePlayer(id = 1L)

        assertIs<Result.Success<Unit>>(result)
        repository.getPlayerById(id = 1L).test {
            val player = awaitItem()
            assertTrue(player?.isHidden == true)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
