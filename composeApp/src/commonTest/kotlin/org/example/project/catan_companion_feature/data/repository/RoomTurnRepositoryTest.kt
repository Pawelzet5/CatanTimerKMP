package org.example.project.catan_companion_feature.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakePlayerDao
import org.example.project.catan_companion_feature.data.fakes.dao.FakeTurnDao
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
import org.example.project.catan_companion_feature.data.local.entity.TurnEntity
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RoomTurnRepositoryTest {

    private val fakeTurnDao = FakeTurnDao()
    private val fakePlayerDao = FakePlayerDao()
    private val repository = RoomTurnRepository(fakeTurnDao, fakePlayerDao)

    @Test
    fun `getTurnsForGame, turns exist for game, returns Flow emitting turns`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 1L, name = "Alice"))
        fakeTurnDao.insert(TurnEntity(id = 1L, gameId = 10L, number = 0, playerId = 1L))
        fakeTurnDao.insert(TurnEntity(id = 2L, gameId = 10L, number = 1, playerId = 1L))

        repository.getTurnsForGame(gameId = 10L).test {
            val turns = awaitItem()
            assertEquals(2, turns.size)
            assertEquals(0, turns[0].number)
            assertEquals(1, turns[1].number)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDiceRoll, turn exists, updates only dice fields`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 1L, name = "Alice"))
        fakeTurnDao.insert(TurnEntity(id = 1L, gameId = 1L, number = 0, playerId = 1L, durationMillis = 5_000L))

        val result = repository.updateDiceRoll(
            turnId = 1L,
            redDice = 3,
            yellowDice = 4,
            eventDice = EventDiceType.BARBARIANS
        )

        assertIs<Result.Success<Unit>>(result)
        repository.getTurnById(id = 1L).test {
            val turn = awaitItem()
            assertEquals(3, turn?.redDice)
            assertEquals(4, turn?.yellowDice)
            assertEquals(EventDiceType.BARBARIANS, turn?.eventDice)
            assertEquals(5_000L, turn?.durationMillis)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDuration, turn exists, updates only durationMillis`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 1L, name = "Alice"))
        fakeTurnDao.insert(
            TurnEntity(id = 1L, gameId = 1L, number = 0, playerId = 1L, redDice = 3, yellowDice = 4)
        )

        val result = repository.updateDuration(turnId = 1L, durationMillis = 90_000L)

        assertIs<Result.Success<Unit>>(result)
        repository.getTurnById(id = 1L).test {
            val turn = awaitItem()
            assertEquals(90_000L, turn?.durationMillis)
            assertEquals(3, turn?.redDice)
            assertEquals(4, turn?.yellowDice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateDiceRoll, turn not found, returns failure`() = runTest {
        val result = repository.updateDiceRoll(
            turnId = 999L,
            redDice = 3,
            yellowDice = 4,
            eventDice = null
        )

        assertIs<Result.Failure<*>>(result)
    }

    @Test
    fun `updateDuration, turn not found, returns failure`() = runTest {
        val result = repository.updateDuration(turnId = 999L, durationMillis = 1_000L)

        assertIs<Result.Failure<*>>(result)
    }

    @Test
    fun `getTurnsForGame, turns from multiple games exist, excludes turns from other games`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 1L, name = "Alice"))
        fakeTurnDao.insert(TurnEntity(id = 1L, gameId = 10L, number = 0, playerId = 1L))
        fakeTurnDao.insert(TurnEntity(id = 2L, gameId = 20L, number = 0, playerId = 1L))

        repository.getTurnsForGame(gameId = 10L).test {
            val turns = awaitItem()
            assertEquals(1, turns.size)
            assertEquals(10L, turns[0].gameId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
