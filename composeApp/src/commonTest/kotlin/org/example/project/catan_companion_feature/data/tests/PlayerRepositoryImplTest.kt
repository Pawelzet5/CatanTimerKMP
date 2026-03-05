package org.example.project.catan_companion_feature.data.tests

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakePlayerDao
import org.example.project.catan_companion_feature.data.repository.PlayerRepositoryImpl
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.makeTestPlayer
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PlayerRepositoryImplTest {

    private lateinit var fakePlayerDao: FakePlayerDao
    private lateinit var repository: PlayerRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakePlayerDao = FakePlayerDao()
        repository = PlayerRepositoryImpl(playerDao = fakePlayerDao)
    }

    // region addPlayer

    @Test
    fun `addPlayer returns success with generated id when insert succeeds`() = runTest {
        // GIVEN
        val player = makeTestPlayer()

        // WHEN
        val result = repository.addPlayer(player)

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(fakePlayerDao.lastInsertedId, result.data)
    }

    @Test
    fun `addPlayer persists player so it can be retrieved afterwards`() = runTest {
        // GIVEN
        val player = makeTestPlayer(id = 1L, name = "Alice")

        // WHEN
        repository.addPlayer(player)

        // THEN
        val fetched = fakePlayerDao.getPlayer(1L)
        assertEquals("Alice", fetched?.name)
    }

    @Test
    fun `addPlayer returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowSQLiteExceptionOnInsert = true

        // WHEN
        val result = repository.addPlayer(makeTestPlayer())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `addPlayer returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowUnexpectedExceptionOnInsert = true

        // WHEN
        val result = repository.addPlayer(makeTestPlayer())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region getPlayer

    @Test
    fun `getPlayer returns success with correct player when player exists`() = runTest {
        // GIVEN
        val player = makeTestPlayer(id = 7L, name = "Bob")
        repository.addPlayer(player)

        // WHEN
        val result = repository.getPlayer(playerId = 7L)

        // THEN
        assertIs<Result.Success<Player>>(result)
        assertEquals("Bob", result.data.name)
    }

    @Test
    fun `getPlayer returns NOT_FOUND when player does not exist`() = runTest {
        // WHEN
        val result = repository.getPlayer(playerId = 999L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `getPlayer returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowSQLiteExceptionOnRead = true

        // WHEN
        val result = repository.getPlayer(playerId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `getPlayer returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowUnexpectedExceptionOnRead = true

        // WHEN
        val result = repository.getPlayer(playerId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region getPlayers

    @Test
    fun `getPlayers emits all players when query is empty`() = runTest {
        // GIVEN
        repository.addPlayer(makeTestPlayer(id = 1L, name = "Alice"))
        repository.addPlayer(makeTestPlayer(id = 2L, name = "Bob"))

        // WHEN / THEN
        repository.getPlayers(query = "").test {
            val players = awaitItem()
            assertEquals(2, players.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPlayers emits filtered players matching query`() = runTest {
        // GIVEN
        repository.addPlayer(makeTestPlayer(id = 1L, name = "Alice"))
        repository.addPlayer(makeTestPlayer(id = 2L, name = "Bob"))

        // WHEN / THEN
        repository.getPlayers(query = "Ali").test {
            val players = awaitItem()
            assertEquals(1, players.size)
            assertEquals("Alice", players.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPlayers emits empty list when no player matches query`() = runTest {
        // GIVEN
        repository.addPlayer(makeTestPlayer(id = 1L, name = "Alice"))

        // WHEN / THEN
        repository.getPlayers(query = "xyz_no_match").test {
            val players = awaitItem()
            assertTrue(players.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPlayers emits updated list when new player is added`() = runTest {
        // WHEN / THEN
        repository.getPlayers(query = "").test {
            assertEquals(0, awaitItem().size)

            repository.addPlayer(makeTestPlayer(id = 1L, name = "Charlie"))

            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region getPlayersForGame

    @Test
    fun `getPlayersForGame returns players associated with given gameId`() = runTest {
        // GIVEN
        val players = listOf(
            makeTestPlayer(id = 1L, name = "Alice"),
            makeTestPlayer(id = 2L, name = "Bob")
        )
        players.forEach { repository.addPlayer(it) }
        fakePlayerDao.setPlayersForGame(gameId = 10L, playerIds = listOf(1L, 2L))

        // WHEN
        val result = repository.getPlayersForGame(gameId = 10L)

        // THEN
        assertIs<Result.Success<List<Player>>>(result)
        assertEquals(2, result.data.size)
    }

    @Test
    fun `getPlayersForGame returns empty list when game has no associated players`() = runTest {
        // WHEN
        val result = repository.getPlayersForGame(gameId = 999L)

        // THEN
        assertIs<Result.Success<List<Player>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getPlayersForGame does not return players from other games`() = runTest {
        // GIVEN
        repository.addPlayer(makeTestPlayer(id = 1L, name = "Alice"))
        repository.addPlayer(makeTestPlayer(id = 2L, name = "Bob"))
        fakePlayerDao.setPlayersForGame(gameId = 1L, playerIds = listOf(1L))
        fakePlayerDao.setPlayersForGame(gameId = 2L, playerIds = listOf(2L))

        // WHEN
        val result = repository.getPlayersForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Player>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("Alice", result.data.first().name)
    }

    @Test
    fun `getPlayersForGame returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowSQLiteExceptionOnGetForGame = true

        // WHEN
        val result = repository.getPlayersForGame(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `getPlayersForGame returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowUnexpectedExceptionOnGetForGame = true

        // WHEN
        val result = repository.getPlayersForGame(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region removePlayer

    @Test
    fun `removePlayer returns success when player is deleted`() = runTest {
        // GIVEN
        val player = makeTestPlayer(id = 5L)
        repository.addPlayer(player)

        // WHEN
        val result = repository.removePlayer(playerId = 5L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `removePlayer makes player no longer retrievable after deletion`() = runTest {
        // GIVEN
        val player = makeTestPlayer(id = 5L)
        repository.addPlayer(player)

        // WHEN
        repository.removePlayer(playerId = 5L)

        // THEN
        val fetched = fakePlayerDao.getPlayer(5L)
        assertEquals(null, fetched)
    }

    @Test
    fun `removePlayer returns UNKNOWN failure when player is linked to a game or turn`() = runTest {
        // GIVEN — simulates Room throwing a RuntimeException wrapping a foreign key constraint
        // violation. SQLiteConstraintException is NOT a subclass of SQLiteException in
        // androidx.sqlite, so tryLocalWrite maps it to UNKNOWN (not DISK_FULL).
        fakePlayerDao.shouldThrowForeignKeyConstraintViolationOnDelete = true

        // WHEN
        val result = repository.removePlayer(playerId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `removePlayer returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakePlayerDao.shouldThrowSQLiteExceptionOnDelete = true

        // WHEN
        val result = repository.removePlayer(playerId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `removePlayer returns success even when player id does not exist`() = runTest {
        // Room's deletePlayer does not return a count, so no NOT_FOUND is expected
        // WHEN
        val result = repository.removePlayer(playerId = 999L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    // endregion
}