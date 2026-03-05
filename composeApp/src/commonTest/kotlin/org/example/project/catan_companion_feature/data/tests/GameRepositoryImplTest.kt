package org.example.project.catan_companion_feature.data.tests

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakeGameDao
import org.example.project.catan_companion_feature.data.fakes.dao.FakePlayerDao
import org.example.project.catan_companion_feature.data.repository.GameRepositoryImpl
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.makeTestGameConfig
import org.example.project.catan_companion_feature.makeTestPlayers
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.*

class GameRepositoryImplTest {

    private lateinit var fakeGameDao: FakeGameDao
    private lateinit var fakePlayerDao: FakePlayerDao
    private lateinit var repository: GameRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakeGameDao = FakeGameDao()
        fakePlayerDao = FakePlayerDao()
        repository = GameRepositoryImpl(gameDao = fakeGameDao, playerDao = fakePlayerDao)
    }

    // region addGame

    @Test
    fun `addGame returns success with generated gameId when insert succeeds`() = runTest {
        // GIVEN
        val config = makeTestGameConfig()

        // WHEN
        val result = repository.addGame(config = config, startedAt = 1000L)

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(fakeGameDao.lastInsertedGameId, result.data)
    }

    @Test
    fun `addGame persists cross refs for all players in config`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val config = makeTestGameConfig(players = players)

        // WHEN
        val result = repository.addGame(config = config, startedAt = 1000L)
        assertIs<Result.Success<Long>>(result)
        val gameId = result.data

        // THEN
        val crossRefs = fakeGameDao.getCrossRefsForGame(gameId)
        assertEquals(3, crossRefs.size)
    }

    @Test
    fun `addGame assigns playerIndex in insertion order`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val config = makeTestGameConfig(players = players)

        // WHEN
        val result = repository.addGame(config = config, startedAt = 1000L)
        assertIs<Result.Success<Long>>(result)
        val gameId = result.data

        // THEN
        val crossRefs = fakeGameDao.getCrossRefsForGame(gameId).sortedBy { it.playerIndex }
        assertEquals(players.map { it.id }, crossRefs.map { it.playerId })
    }

    @Test
    fun `addGame persists startedAt timestamp`() = runTest {
        // GIVEN
        val startedAt = 9999L

        // WHEN
        val result = repository.addGame(config = makeTestGameConfig(), startedAt = startedAt)
        assertIs<Result.Success<Long>>(result)

        // THEN
        val stored = fakeGameDao.getGame(result.data)
        assertEquals(startedAt, stored?.startedAt)
    }

    @Test
    fun `addGame returns failure when dao throws SQLiteException on game insert`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowSQLiteExceptionOnInsert = true

        // WHEN
        val result = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `addGame returns failure when dao throws SQLiteException on cross ref insert`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowSQLiteExceptionOnCrossRefInsert = true

        // WHEN
        val result = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `addGame returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowUnexpectedExceptionOnInsert = true

        // WHEN
        val result = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region getGame

    @Test
    fun `getGame returns success with correct game when game exists`() = runTest {
        // GIVEN
        val config = makeTestGameConfig()
        val addResult = repository.addGame(config = config, startedAt = 5000L)
        assertIs<Result.Success<Long>>(addResult)
        val gameId = addResult.data
        fakePlayerDao.setPlayersForGame(gameId = gameId, playerIds = config.players.map { it.id })

        // WHEN
        val result = repository.getGame(gameId = gameId)

        // THEN
        assertIs<Result.Success<Game>>(result)
        assertEquals(gameId, result.data.id)
    }

    @Test
    fun `getGame returns NOT_FOUND when game does not exist`() = runTest {
        // WHEN
        val result = repository.getGame(gameId = 999L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `getGame returns game with ordered players matching playerDao order`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val config = makeTestGameConfig(players = players)
        val addResult = repository.addGame(config = config, startedAt = 1000L)
        assertIs<Result.Success<Long>>(addResult)
        val gameId = addResult.data
        // Simulate playerDao returning players in a specific order (e.g. by playerIndex)
        fakePlayerDao.setPlayersForGame(gameId = gameId, playerIds = players.map { it.id })

        // WHEN
        val result = repository.getGame(gameId = gameId)

        // THEN
        assertIs<Result.Success<Game>>(result)
        assertEquals(players.map { it.id }, result.data.config.players.map { it.id })
    }

    @Test
    fun `getGame returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowSQLiteExceptionOnRead = true

        // WHEN
        val result = repository.getGame(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `getGame returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowUnexpectedExceptionOnRead = true

        // WHEN
        val result = repository.getGame(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region getGameSummaries

    @Test
    fun `getGameSummaries emits empty list when no games exist`() = runTest {
        // WHEN / THEN
        repository.getGameSummaries().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGameSummaries emits one summary per persisted game`() = runTest {
        // GIVEN
        repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)
        repository.addGame(config = makeTestGameConfig(), startedAt = 2000L)

        // WHEN / THEN
        repository.getGameSummaries().test {
            val summaries = awaitItem()
            assertEquals(2, summaries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGameSummaries emits updated list after new game is added`() = runTest {
        // WHEN / THEN
        repository.getGameSummaries().test {
            assertEquals(0, awaitItem().size)

            repository.addGame(config = makeTestGameConfig(), startedAt = 3000L)

            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region getActiveGame

    @Test
    fun `getActiveGame returns NOT_FOUND as long as implementation is pending`() = runTest {
        // This is a documented TODO — verifying the stub returns a safe default
        // WHEN
        val result = repository.getActiveGame()

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    // endregion

    // region saveGameAsFinished

    @Test
    fun `saveGameAsFinished returns success when game exists and status is updated`() = runTest {
        // GIVEN
        val addResult = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)
        assertIs<Result.Success<Long>>(addResult)
        val gameId = addResult.data

        // WHEN
        val result = repository.saveGameAsFinished(gameId = gameId, finishedAt = 9000L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `saveGameAsFinished persists finishedAt timestamp`() = runTest {
        // GIVEN
        val addResult = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)
        assertIs<Result.Success<Long>>(addResult)
        val gameId = addResult.data
        val finishedAt = 88_000L

        // WHEN
        repository.saveGameAsFinished(gameId = gameId, finishedAt = finishedAt)

        // THEN
        val stored = fakeGameDao.getGame(gameId)
        assertEquals(finishedAt, stored?.finishedAt)
    }

    @Test
    fun `saveGameAsFinished returns NOT_FOUND when no rows were affected`() = runTest {
        // GIVEN — game never inserted
        fakeGameDao.updateStatusRowCount = 0

        // WHEN
        val result = repository.saveGameAsFinished(gameId = 999L, finishedAt = 9000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `saveGameAsFinished returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowSQLiteExceptionOnStatusUpdate = true

        // WHEN
        val result = repository.saveGameAsFinished(gameId = 1L, finishedAt = 9000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `saveGameAsFinished returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakeGameDao.shouldThrowUnexpectedExceptionOnStatusUpdate = true

        // WHEN
        val result = repository.saveGameAsFinished(gameId = 1L, finishedAt = 9000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion
}