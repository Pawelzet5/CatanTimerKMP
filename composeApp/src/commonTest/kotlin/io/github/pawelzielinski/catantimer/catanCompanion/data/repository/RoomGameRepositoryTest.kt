package io.github.pawelzielinski.catantimer.catanCompanion.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.dao.FakeGameDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.dao.FakeGamePlayerDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.dao.FakePlayerDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GameEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GamePlayerEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.PlayerEntity
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameStatus
import io.github.pawelzielinski.catantimer.core.data.TransactionRunner
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomGameRepositoryTest {

    private val fakeGameDao = FakeGameDao()
    private val fakeGamePlayerDao = FakeGamePlayerDao()
    private val fakePlayerDao = FakePlayerDao()
    private val repository = RoomGameRepository(
        transactionRunner = object : TransactionRunner {
            override suspend fun <T> run(block: suspend () -> T): T = block()
        },
        gameDao = fakeGameDao,
        gamePlayerDao = fakeGamePlayerDao,
        playerDao = fakePlayerDao
    )

    private val baseGame = GameEntity(
        id = 1L,
        turnDurationMillis = 60_000L,
        expansions = emptySet(),
        specialTurnRuleEnabled = false,
        status = GameStatus.IN_PROGRESS,
        startedAt = 1000L
    )

    // region query methods

    @Test
    fun `getAllGames, emits all games`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L))
        fakeGameDao.insert(baseGame.copy(id = 2L))

        // WHEN
        repository.getAllGames().test {
            // THEN
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInProgressGames, mixed statuses, emits only in-progress games`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.IN_PROGRESS))
        fakeGameDao.insert(baseGame.copy(id = 2L, status = GameStatus.COMPLETED))

        // WHEN
        repository.getInProgressGames().test {
            // THEN
            val games = awaitItem()
            assertEquals(1, games.size)
            assertEquals(GameStatus.IN_PROGRESS, games[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCompletedGames, mixed statuses, emits only completed games`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.IN_PROGRESS))
        fakeGameDao.insert(baseGame.copy(id = 2L, status = GameStatus.COMPLETED))

        // WHEN
        repository.getCompletedGames().test {
            // THEN
            val games = awaitItem()
            assertEquals(1, games.size)
            assertEquals(GameStatus.COMPLETED, games[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGameById, game has players, emits game with mapped player names`() = runTest {
        // GIVEN
        fakePlayerDao.addPlayers(PlayerEntity(id = 10L, name = "Alice"))
        fakeGameDao.insert(baseGame.copy(id = 1L))
        fakeGamePlayerDao.insertAll(listOf(GamePlayerEntity(gameId = 1L, playerId = 10L, orderIndex = 0)))

        // WHEN
        repository.getGameById(1L).test {
            // THEN
            val game = awaitItem()
            assertEquals(1L, game?.id)
            assertEquals(1, game?.players?.size)
            assertEquals("Alice", game?.players?.get(0)?.playerName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGameById, game does not exist, emits null`() = runTest {
        // WHEN
        repository.getGameById(999L).test {
            // THEN
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMostRecentInProgressGame, multiple in-progress games, emits most recent by startedAt`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L, startedAt = 100L, status = GameStatus.IN_PROGRESS))
        fakeGameDao.insert(baseGame.copy(id = 2L, startedAt = 200L, status = GameStatus.IN_PROGRESS))

        // WHEN
        repository.getMostRecentInProgressGame().test {
            // THEN
            assertEquals(2L, awaitItem()?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMostRecentInProgressGame, no in-progress games, emits null`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.COMPLETED))

        // WHEN
        repository.getMostRecentInProgressGame().test {
            // THEN
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region write methods

    @Test
    fun `createGame, valid players, inserts game and game-player associations`() = runTest {
        // GIVEN
        fakePlayerDao.addPlayers(
            PlayerEntity(id = 1L, name = "Alice"),
            PlayerEntity(id = 2L, name = "Bob"),
            PlayerEntity(id = 3L, name = "Charlie")
        )

        // WHEN
        val result = repository.createGame(
            turnDurationMillis = 60_000L,
            expansions = setOf(GameExpansion.SEAFARERS),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 3L)
        )

        // THEN
        assertIs<Result.Success<Long>>(result)
        val gameId = result.data
        repository.getGameById(gameId).test {
            val game = awaitItem()
            assertEquals(GameStatus.IN_PROGRESS, game?.status)
            assertEquals(setOf(GameExpansion.SEAFARERS), game?.expansions)
            assertEquals(3, game?.players?.size)
            assertEquals(0, game?.players?.get(0)?.orderIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateGameSettings, updates expansions and specialTurnRuleEnabled`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L, expansions = emptySet(), specialTurnRuleEnabled = false))

        // WHEN
        val result = repository.updateGameSettings(
            gameId = 1L,
            expansions = setOf(GameExpansion.SEAFARERS),
            specialTurnRuleEnabled = true
        )

        // THEN
        assertIs<Result.Success<Unit>>(result)
        repository.getGameById(1L).test {
            val game = awaitItem()
            assertTrue(game?.expansions?.contains(GameExpansion.SEAFARERS) == true)
            assertEquals(true, game?.specialTurnRuleEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateGameSettings, game not found, returns NOT_FOUND failure`() = runTest {
        // WHEN
        val result = repository.updateGameSettings(
            gameId = 999L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false
        )

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `endGame, marks as completed and sets winner`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.IN_PROGRESS))

        // WHEN
        val result = repository.endGame(gameId = 1L, winnerId = 42L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        repository.getGameById(1L).test {
            val game = awaitItem()
            assertEquals(GameStatus.COMPLETED, game?.status)
            assertEquals(42L, game?.winnerId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `endGame, game not found, returns NOT_FOUND failure`() = runTest {
        // WHEN
        val result = repository.endGame(gameId = 999L, winnerId = null)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `deleteGame, removes it`() = runTest {
        // GIVEN
        fakeGameDao.insert(baseGame.copy(id = 1L))

        // WHEN
        val result = repository.deleteGame(id = 1L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        repository.getGameById(1L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteGame, game not found, returns NOT_FOUND failure`() = runTest {
        // WHEN
        val result = repository.deleteGame(id = 999L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    // endregion
}
