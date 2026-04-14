package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakeTurnDao
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.makeTestTurn
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TurnRepositoryImplTest {

    private lateinit var fakeTurnDao: FakeTurnDao
    private lateinit var repository: TurnRepositoryImpl

    @BeforeTest
    fun setUp() {
        fakeTurnDao = FakeTurnDao()
        repository = TurnRepositoryImpl(turnDao = fakeTurnDao)
    }

    // region addTurn

    @Test
    fun `addTurn returns success with generated id when insert succeeds`() = runTest {
        // WHEN
        val result = repository.addTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(fakeTurnDao.lastInsertedId, result.data)
    }

    @Test
    fun `addTurn persists turn associated with given gameId`() = runTest {
        // GIVEN
        val turn = makeTestTurn(number = 0)

        // WHEN
        repository.addTurn(gameId = 42L, turn = turn)

        // THEN
        val stored = fakeTurnDao.getTurnsForGame(42L)
        assertEquals(1, stored.size)
        assertEquals(turn.number, stored.first().number)
    }

    @Test
    fun `addTurn does not persist turn under different gameId`() = runTest {
        // GIVEN
        repository.addTurn(gameId = 1L, turn = makeTestTurn())

        // WHEN
        val storedForOtherGame = fakeTurnDao.getTurnsForGame(2L)

        // THEN
        assertTrue(storedForOtherGame.isEmpty())
    }

    // endregion

    // region updateTurn

    @Test
    fun `updateTurn returns success when turn exists and update succeeds`() = runTest {
        // GIVEN
        val turn = makeTestTurn()
        repository.addTurn(gameId = 1L, turn = turn)
        val insertedId = fakeTurnDao.lastInsertedId

        // WHEN
        val result = repository.updateTurn(gameId = 1L, turn = turn.copy(id = insertedId, durationMillis = 90_000L))

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `updateTurn persists changed fields`() = runTest {
        // GIVEN
        val turn = makeTestTurn()
        repository.addTurn(gameId = 1L, turn = turn)
        val insertedId = fakeTurnDao.lastInsertedId
        val newDuration = 75_000L

        // WHEN
        repository.updateTurn(gameId = 1L, turn = turn.copy(id = insertedId, durationMillis = newDuration))

        // THEN
        val stored = fakeTurnDao.getTurnsForGame(1L).first()
        assertEquals(newDuration, stored.durationMillis)
    }

    @Test
    fun `updateTurn returns NOT_FOUND when no rows were affected`() = runTest {
        // GIVEN — dao configured to report 0 updated rows
        fakeTurnDao.updatedRowCount = 0

        // WHEN
        val result = repository.updateTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    // endregion

    // region getTurnsForGame

    @Test
    fun `getTurnsForGame returns empty list when no turns exist for game`() = runTest {
        // WHEN
        val result = repository.getTurnsForGame(gameId = 99L)

        // THEN
        assertIs<Result.Success<List<Turn>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun `getTurnsForGame returns only turns belonging to requested gameId`() = runTest {
        // GIVEN
        repository.addTurn(gameId = 1L, turn = makeTestTurn(number = 0))
        repository.addTurn(gameId = 1L, turn = makeTestTurn(number = 1))
        repository.addTurn(gameId = 2L, turn = makeTestTurn(number = 0))

        // WHEN
        val result = repository.getTurnsForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Turn>>>(result)
        assertEquals(2, result.data.size)
    }

    @Test
    fun `getTurnsForGame returns turns sorted by number ascending`() = runTest {
        // GIVEN — inserted out of order intentionally
        repository.addTurn(gameId = 1L, turn = makeTestTurn(number = 2))
        repository.addTurn(gameId = 1L, turn = makeTestTurn(number = 0))
        repository.addTurn(gameId = 1L, turn = makeTestTurn(number = 1))

        // WHEN
        val result = repository.getTurnsForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Turn>>>(result)
        assertEquals(listOf(0, 1, 2), result.data.map { it.number })
    }

    // endregion
}