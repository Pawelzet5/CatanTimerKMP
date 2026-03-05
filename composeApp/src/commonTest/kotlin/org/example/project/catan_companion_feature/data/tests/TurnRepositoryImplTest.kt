package org.example.project.catan_companion_feature.data.tests

import kotlinx.coroutines.test.runTest
import org.example.project.catan_companion_feature.data.fakes.dao.FakeTurnDao
import org.example.project.catan_companion_feature.data.repository.TurnRepositoryImpl
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.makeTestTurn
import org.example.project.core.domain.DataError
import org.example.project.core.domain.Result
import kotlin.test.*

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
        // GIVEN
        val turn = makeTestTurn()

        // WHEN
        val result = repository.addTurn(gameId = 1L, turn = turn)

        // THEN
        assertIs<Result.Success<Long>>(result)
        assertEquals(fakeTurnDao.lastInsertedId, result.data)
    }

    @Test
    fun `addTurn persists turn associated with given gameId`() = runTest {
        // GIVEN
        val turn = makeTestTurn()
        val gameId = 42L

        // WHEN
        repository.addTurn(gameId = gameId, turn = turn)

        // THEN
        val stored = fakeTurnDao.getTurnsForGame(gameId)
        assertEquals(1, stored.size)
        assertEquals(turn.number, stored.first().number)
    }

    @Test
    fun `addTurn returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakeTurnDao.shouldThrowSQLiteExceptionOnInsert = true

        // WHEN
        val result = repository.addTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        // SQLiteException during write maps to DISK_FULL per tryLocalWrite contract
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `addTurn returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakeTurnDao.shouldThrowUnexpectedExceptionOnInsert = true

        // WHEN
        val result = repository.addTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region updateTurn

    @Test
    fun `updateTurn returns success when turn exists and update succeeds`() = runTest {
        // GIVEN
        val turn = makeTestTurn()
        repository.addTurn(gameId = 1L, turn = turn)

        // WHEN
        val updated = turn.copy(durationMillis = 90_000L)
        val result = repository.updateTurn(gameId = 1L, turn = updated)

        // THEN
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `updateTurn persists changed fields when update succeeds`() = runTest {
        // GIVEN
        val turn = makeTestTurn()
        repository.addTurn(gameId = 1L, turn = turn)
        val newDuration = 75_000L

        // WHEN
        repository.updateTurn(gameId = 1L, turn = turn.copy(durationMillis = newDuration))

        // THEN
        val stored = fakeTurnDao.getTurnsForGame(1L).first()
        assertEquals(newDuration, stored.durationMillis)
    }

    @Test
    fun `updateTurn returns NOT_FOUND when no rows were affected`() = runTest {
        // GIVEN
        fakeTurnDao.updatedRowCount = 0

        // WHEN
        val result = repository.updateTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, result.error)
    }

    @Test
    fun `updateTurn returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakeTurnDao.shouldThrowSQLiteExceptionOnUpdate = true

        // WHEN
        val result = repository.updateTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.DISK_FULL, result.error)
    }

    @Test
    fun `updateTurn returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakeTurnDao.shouldThrowUnexpectedExceptionOnUpdate = true

        // WHEN
        val result = repository.updateTurn(gameId = 1L, turn = makeTestTurn())

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion

    // region getTurnsForGame

    @Test
    fun `getTurnsForGame returns empty list when no turns exist for game`() = runTest {
        // WHEN
        val result = repository.getTurnsForGame(gameId = 99L)

        // THEN
        assertIs<Result.Success<List<Turn>>>(result)
        assertEquals(emptyList(), result.data)
    }

    @Test
    fun `getTurnsForGame returns only turns belonging to requested gameId`() = runTest {
        // GIVEN
        repository.addTurn(gameId = 1L, turn = makeTestTurn(id = 1L, number = 0))
        repository.addTurn(gameId = 1L, turn = makeTestTurn(id = 2L, number = 1))
        repository.addTurn(gameId = 2L, turn = makeTestTurn(id = 3L, number = 0))

        // WHEN
        val result = repository.getTurnsForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Turn>>>(result)
        assertEquals(2, result.data.size)
        assertTrue(result.data.all { it.number in listOf(0, 1) })
    }

    @Test
    fun `getTurnsForGame returns all turns in insertion order`() = runTest {
        // GIVEN
        val turns = listOf(
            makeTestTurn(id = 1L, number = 0),
            makeTestTurn(id = 2L, number = 1),
            makeTestTurn(id = 3L, number = 2)
        )
        turns.forEach { repository.addTurn(gameId = 1L, turn = it) }

        // WHEN
        val result = repository.getTurnsForGame(gameId = 1L)

        // THEN
        assertIs<Result.Success<List<Turn>>>(result)
        assertEquals(listOf(0, 1, 2), result.data.map { it.number })
    }

    @Test
    fun `getTurnsForGame returns failure when dao throws SQLiteException`() = runTest {
        // GIVEN
        fakeTurnDao.shouldThrowSQLiteExceptionOnRead = true

        // WHEN
        val result = repository.getTurnsForGame(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    @Test
    fun `getTurnsForGame returns failure when dao throws unexpected exception`() = runTest {
        // GIVEN
        fakeTurnDao.shouldThrowUnexpectedExceptionOnRead = true

        // WHEN
        val result = repository.getTurnsForGame(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.UNKNOWN, result.error)
    }

    // endregion
}