package org.example.project.catan_companion_feature.data.fakes.dao

import androidx.sqlite.SQLiteException
import org.example.project.catan_companion_feature.data.local.dao.TurnDao
import org.example.project.catan_companion_feature.data.local.entity.TurnEntity

class FakeTurnDao : TurnDao {

    private val _turns = mutableListOf<TurnEntity>()
    val turns: List<TurnEntity> get() = _turns.toList()

    // region error flags

    var shouldThrowSQLiteExceptionOnInsert = false
    var shouldThrowUnexpectedExceptionOnInsert = false

    var shouldThrowSQLiteExceptionOnUpdate = false
    var shouldThrowUnexpectedExceptionOnUpdate = false

    var shouldThrowSQLiteExceptionOnRead = false
    var shouldThrowUnexpectedExceptionOnRead = false

    // endregion

    // region state controls

    /** Overrides the number of rows reported as updated. -1 means "use real count". */
    var updatedRowCount: Int = -1

    var lastInsertedId: Long = 0L
        private set

    private var nextId = 1L

    /**
     * Prepopulates the DAO with existing entities (e.g. for repository restore tests).
     * IDs are set by the caller — nextId is advanced to avoid future collisions.
     */
    fun addTurns(vararg turns: TurnEntity) {
        _turns.addAll(turns)
        nextId = (_turns.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    // endregion

    // region TurnDao impl

    override suspend fun insertTurn(turn: TurnEntity): Long {
        if (shouldThrowSQLiteExceptionOnInsert) throw SQLiteException("Fake: disk full")
        if (shouldThrowUnexpectedExceptionOnInsert) throw RuntimeException("Fake: unexpected error")

        val id = nextId++
        _turns.add(turn.copy(id = id))
        lastInsertedId = id
        return id
    }

    override suspend fun updateTurn(turn: TurnEntity): Int {
        if (shouldThrowSQLiteExceptionOnUpdate) throw SQLiteException("Fake: disk full")
        if (shouldThrowUnexpectedExceptionOnUpdate) throw RuntimeException("Fake: unexpected error")

        if (updatedRowCount != -1) return updatedRowCount

        val index = _turns.indexOfFirst { it.id == turn.id }
        if (index == -1) return 0
        _turns[index] = turn
        return 1
    }

    override suspend fun getLastTurn(gameId: Long): TurnEntity? {
        if (shouldThrowSQLiteExceptionOnRead) throw SQLiteException("Fake: read error")
        if (shouldThrowUnexpectedExceptionOnRead) throw RuntimeException("Fake: unexpected error")

        return _turns
            .filter { it.gameId == gameId }
            .maxByOrNull { it.number }
    }

    override suspend fun getTurnsForGame(gameId: Long): List<TurnEntity> {
        if (shouldThrowSQLiteExceptionOnRead) throw SQLiteException("Fake: read error")
        if (shouldThrowUnexpectedExceptionOnRead) throw RuntimeException("Fake: unexpected error")

        return _turns
            .filter { it.gameId == gameId }
            .sortedBy { it.number }
    }

    // endregion
}