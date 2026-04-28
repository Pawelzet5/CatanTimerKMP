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
    fun `getAllGames, games exist, emits all games`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L))
        fakeGameDao.insert(baseGame.copy(id = 2L))

        repository.getAllGames().test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getInProgressGames, mixed statuses, emits only in-progress games`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.IN_PROGRESS))
        fakeGameDao.insert(baseGame.copy(id = 2L, status = GameStatus.COMPLETED))

        repository.getInProgressGames().test {
            val games = awaitItem()
            assertEquals(1, games.size)
            assertEquals(GameStatus.IN_PROGRESS, games[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCompletedGames, mixed statuses, emits only completed games`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.IN_PROGRESS))
        fakeGameDao.insert(baseGame.copy(id = 2L, status = GameStatus.COMPLETED))

        repository.getCompletedGames().test {
            val games = awaitItem()
            assertEquals(1, games.size)
            assertEquals(GameStatus.COMPLETED, games[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGameById, game exists with players, emits game with mapped player names`() = runTest {
        fakePlayerDao.addPlayers(PlayerEntity(id = 10L, name = "Alice"))
        fakeGameDao.insert(baseGame.copy(id = 1L))
        fakeGamePlayerDao.insertAll(listOf(GamePlayerEntity(gameId = 1L, playerId = 10L, orderIndex = 0)))

        repository.getGameById(1L).test {
            val game = awaitItem()
            assertEquals(1L, game?.id)
            assertEquals(1, game?.players?.size)
            assertEquals("Alice", game?.players?.get(0)?.playerName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGameById, game does not exist, emits null`() = runTest {
        repository.getGameById(999L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMostRecentInProgressGame, multiple in-progress games, emits most recent by startedAt`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L, startedAt = 100L, status = GameStatus.IN_PROGRESS))
        fakeGameDao.insert(baseGame.copy(id = 2L, startedAt = 200L, status = GameStatus.IN_PROGRESS))

        repository.getMostRecentInProgressGame().test {
            assertEquals(2L, awaitItem()?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMostRecentInProgressGame, no in-progress games, emits null`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.COMPLETED))

        repository.getMostRecentInProgressGame().test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region write methods

    @Test
    fun `createGame, valid players, inserts game and game-player associations`() = runTest {
        fakePlayerDao.addPlayers(
            PlayerEntity(id = 1L, name = "Alice"),
            PlayerEntity(id = 2L, name = "Bob"),
            PlayerEntity(id = 3L, name = "Charlie")
        )

        val result = repository.createGame(
            turnDurationMillis = 60_000L,
            expansions = setOf(GameExpansion.SEAFARERS),
            specialTurnRuleEnabled = false,
            playerIds = listOf(1L, 2L, 3L)
        )

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
    fun `updateGameSettings, game exists, updates expansions and specialTurnRuleEnabled`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L, expansions = emptySet(), specialTurnRuleEnabled = false))

        val result = repository.updateGameSettings(
            gameId = 1L,
            expansions = setOf(GameExpansion.SEAFARERS),
            specialTurnRuleEnabled = true
        )

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
        val result = repository.updateGameSettings(
            gameId = 999L,
            expansions = emptySet(),
            specialTurnRuleEnabled = false
        )

        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `endGame, game exists, marks as completed and sets winner`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L, status = GameStatus.IN_PROGRESS))

        val result = repository.endGame(gameId = 1L, winnerId = 42L)

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
        val result = repository.endGame(gameId = 999L, winnerId = null)

        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `deleteGame, game exists, removes it`() = runTest {
        fakeGameDao.insert(baseGame.copy(id = 1L))

        val result = repository.deleteGame(id = 1L)

        assertIs<Result.Success<Unit>>(result)
        repository.getGameById(1L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteGame, game not found, returns NOT_FOUND failure`() = runTest {
        val result = repository.deleteGame(id = 999L)

        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    // endregion
}
