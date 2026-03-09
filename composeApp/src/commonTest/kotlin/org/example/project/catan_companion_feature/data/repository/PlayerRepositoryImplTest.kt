package org.example.project.catan_companion_feature.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakePlayerDao
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.makeTestPlayer
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.*

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
        // WHEN
        val result = repository.addPlayer(makeTestPlayer())

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(fakePlayerDao.lastInsertedId, result.data)
    }

    @Test
    fun `addPlayer persists player so it can be retrieved afterwards`() = runTest {
        // GIVEN
        val player = makeTestPlayer(name = "Alice")

        // WHEN
        val result = repository.addPlayer(player)
        assertIs<Result.Success<Long>>(result)

        // THEN
        val fetched = fakePlayerDao.getPlayer(result.data)
        assertEquals("Alice", fetched?.name)
    }

    // endregion

    // region getPlayer

    @Test
    fun `getPlayer returns success with correct player when player exists`() = runTest {
        // GIVEN
        val addResult = repository.addPlayer(makeTestPlayer(name = "Bob"))
        assertIs<Result.Success<Long>>(addResult)

        // WHEN
        val result = repository.getPlayer(playerId = addResult.data)

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

    // endregion

    // region getPlayers

    @Test
    fun `getPlayers emits all players when query is empty`() = runTest {
        // GIVEN
        repository.addPlayer(makeTestPlayer(name = "Alice"))
        repository.addPlayer(makeTestPlayer(name = "Bob"))

        // WHEN / THEN
        repository.getPlayers(query = "").test {
            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPlayers emits only players matching query`() = runTest {
        // GIVEN
        repository.addPlayer(makeTestPlayer(name = "Alice"))
        repository.addPlayer(makeTestPlayer(name = "Bob"))

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
        repository.addPlayer(makeTestPlayer(name = "Alice"))

        // WHEN / THEN
        repository.getPlayers(query = "xyz_no_match").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPlayers emits updated list when new player is added`() = runTest {
        // WHEN / THEN
        repository.getPlayers(query = "").test {
            assertEquals(0, awaitItem().size)

            repository.addPlayer(makeTestPlayer(name = "Charlie"))

            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region getPlayersForGame

    @Test
    fun `getPlayersForGame returns players associated with given gameId`() = runTest {
        // GIVEN — używamy id zwróconych przez addPlayer, nie z obiektu domenowego
        val aliceId = (repository.addPlayer(makeTestPlayer(name = "Alice")) as Result.Success).data
        val bobId = (repository.addPlayer(makeTestPlayer(name = "Bob")) as Result.Success).data
        fakePlayerDao.setPlayersForGame(gameId = 10L, playerIds = listOf(aliceId, bobId))

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
    fun `getPlayersForGame returns players in playerIndex order`() = runTest {
        // GIVEN
        val aliceId = (repository.addPlayer(makeTestPlayer(name = "Alice")) as Result.Success).data
        val bobId = (repository.addPlayer(makeTestPlayer(name = "Bob")) as Result.Success).data
        val charlieId =
            (repository.addPlayer(makeTestPlayer(name = "Charlie")) as Result.Success).data
        // Bob first, then Alice, then Charlie — arbitrary game-specific order
        fakePlayerDao.setPlayersForGame(gameId = 1L, playerIds = listOf(bobId, aliceId, charlieId))

        // WHEN
        val result = repository.getPlayersForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Player>>>(result)
        assertEquals(listOf("Bob", "Alice", "Charlie"), result.data.map { it.name })
    }

    @Test
    fun `getPlayersForGame does not return players from other games`() = runTest {
        // GIVEN
        val aliceId = (repository.addPlayer(makeTestPlayer(name = "Alice")) as Result.Success).data
        val bobId = (repository.addPlayer(makeTestPlayer(name = "Bob")) as Result.Success).data
        fakePlayerDao.setPlayersForGame(gameId = 1L, playerIds = listOf(aliceId))
        fakePlayerDao.setPlayersForGame(gameId = 2L, playerIds = listOf(bobId))

        // WHEN
        val result = repository.getPlayersForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Player>>>(result)
        assertEquals(1, result.data.size)
        assertEquals("Alice", result.data.first().name)
    }

    // endregion

    // region removePlayer

    @Test
    fun `removePlayer returns success when player is deleted`() = runTest {
        // GIVEN
        val addResult = repository.addPlayer(makeTestPlayer())
        assertIs<Result.Success<Long>>(addResult)

        // WHEN
        val result = repository.removePlayer(playerId = addResult.data)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `removePlayer makes player no longer retrievable`() = runTest {
        // GIVEN
        val addResult = repository.addPlayer(makeTestPlayer())
        assertIs<Result.Success<Long>>(addResult)

        // WHEN
        repository.removePlayer(playerId = addResult.data)

        // THEN
        val fetched = fakePlayerDao.getPlayer(addResult.data)
        assertEquals(null, fetched)
    }

    @Test
    fun `removePlayer returns success even when player id does not exist`() = runTest {
        // Room's deletePlayer does not return a row count — no NOT_FOUND is expected
        // WHEN
        val result = repository.removePlayer(playerId = 999L)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `removePlayer returns UNKNOWN failure when player is linked to a game or turn`() = runTest {
        // GIVEN — simulates Room throwing SQLiteConstraintException (mapped to UNKNOWN by tryLocalWrite)
        fakePlayerDao.shouldThrowConstraintExceptionOnDelete = true

        // WHEN
        val result = repository.removePlayer(playerId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion
}