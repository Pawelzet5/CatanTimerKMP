package org.example.project.catan_companion_feature.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakeGameDao
import org.example.project.catan_companion_feature.data.fakes.dao.FakePlayerDao
import org.example.project.catan_companion_feature.data.local.mapper.toEntity
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
    fun `addGame returns success with generated gameId`() = runTest {
        // WHEN
        val result = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(fakeGameDao.lastInsertedGameId, result.data)
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
    fun `addGame persists cross refs for all players in config`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val config = makeTestGameConfig(players = players)

        // WHEN
        val result = repository.addGame(config = config, startedAt = 1000L)
        assertIs<Result.Success<Long>>(result)

        // THEN
        val crossRefs = fakeGameDao.getCrossRefsForGame(result.data)
        assertEquals(3, crossRefs.size)
    }

    @Test
    fun `addGame assigns playerIndex in config list order`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val config = makeTestGameConfig(players = players)

        // WHEN
        val result = repository.addGame(config = config, startedAt = 1000L)
        assertIs<Result.Success<Long>>(result)

        // THEN
        val crossRefs = fakeGameDao.getCrossRefsForGame(result.data).sortedBy { it.playerIndex }
        assertEquals(players.map { it.id }, crossRefs.map { it.playerId })
    }

    // endregion

    // region getGame

    @Test
    fun `getGame returns success with correct game when game exists`() = runTest {
        // GIVEN
        val config = makeTestGameConfig()
        val addResult = repository.addGame(config = config, startedAt = 5000L)
        assertIs<Result.Success<Long>>(addResult)
        fakePlayerDao.addPlayers(*config.players.map { it.toEntity() }.toTypedArray())
        fakePlayerDao.setPlayersForGame(
            gameId = addResult.data,
            playerIds = config.players.map { it.id }
        )

        // WHEN
        val result = repository.getGame(gameId = addResult.data)

        // THEN
        assertIs<Result.Success<Game>>(result)
        assertEquals(addResult.data, result.data.id)
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
    fun `getGame returns players in order defined by playerDao`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val config = makeTestGameConfig(players = players)
        val addResult = repository.addGame(config = config, startedAt = 1000L)
        assertIs<Result.Success<Long>>(addResult)
        fakePlayerDao.addPlayers(*players.map { it.toEntity() }.toTypedArray())
        // Reverse order to verify repository respects playerDao ordering, not insertion order
        fakePlayerDao.setPlayersForGame(
            gameId = addResult.data,
            playerIds = players.map { it.id }.reversed()
        )

        // WHEN
        val result = repository.getGame(gameId = addResult.data)

        // THEN
        assertIs<Result.Success<Game>>(result)
        assertEquals(players.map { it.id }.reversed(), result.data.config.players.map { it.id })
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
            assertEquals(2, awaitItem().size)
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

    @Test
    fun `getGameSummaries emits summaries ordered by id descending`() = runTest {
        // GIVEN
        val r1 = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)
        val r2 = repository.addGame(config = makeTestGameConfig(), startedAt = 2000L)
        assertIs<Result.Success<Long>>(r1)
        assertIs<Result.Success<Long>>(r2)

        // WHEN / THEN
        repository.getGameSummaries().test {
            val ids = awaitItem().map { it.id }
            assertEquals(listOf(r2.data, r1.data), ids)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region getActiveGame

    @Test
    fun `getActiveGame returns NOT_FOUND as long as implementation is pending`() = runTest {
        // Guards stub from TODO hidden change of implementation
        // WHEN
        val result = repository.getActiveGame()

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    // endregion

    // region saveGameAsFinished

    @Test
    fun `saveGameAsFinished returns success when game exists`() = runTest {
        // GIVEN
        val addResult = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)
        assertIs<Result.Success<Long>>(addResult)

        // WHEN
        val result = repository.saveGameAsFinished(gameId = addResult.data, finishedAt = 9000L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `saveGameAsFinished persists finishedAt timestamp`() = runTest {
        // GIVEN
        val addResult = repository.addGame(config = makeTestGameConfig(), startedAt = 1000L)
        assertIs<Result.Success<Long>>(addResult)
        val finishedAt = 88_000L

        // WHEN
        repository.saveGameAsFinished(gameId = addResult.data, finishedAt = finishedAt)

        // THEN
        val stored = fakeGameDao.getGame(addResult.data)
        assertEquals(finishedAt, stored?.finishedAt)
    }

    @Test
    fun `saveGameAsFinished returns NOT_FOUND when no rows were affected`() = runTest {
        // GIVEN — game never inserted, dao configured to report 0 updated rows
        fakeGameDao.updateStatusRowCount = 0

        // WHEN
        val result = repository.saveGameAsFinished(gameId = 999L, finishedAt = 9000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    // endregion
}