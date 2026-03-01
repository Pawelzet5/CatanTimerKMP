package org.example.project.catan_companion_feature.domain.repository

import org.example.project.core.util.DataError
import org.example.project.core.util.EmptyResult
import org.example.project.core.util.Result
import org.example.project.catan_companion_feature.domain.dataclass.Turn

class FakeTurnRepository : TurnRepository {

    private val _turns = mutableListOf<Turn>()
    val turns: List<Turn> get() = _turns.toList()

    var shouldFailOnAdd = false
    var shouldFailOnUpdate = false
    var shouldFailOnGetAll = false

    private var nextId = 1L

    /**
     * Prepopulates the repository with existing turns (e.g. for session restore tests).
     * Bypasses the id-generation logic intentionally – ids are set by the caller.
     */
    fun addTurns(vararg turns: Turn) {
        _turns.addAll(turns)
        // keep nextId ahead of any manually inserted ids to avoid collisions
        nextId = (_turns.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    override suspend fun addTurn(turn: Turn): Result<Long, DataError.Local> {
        if (shouldFailOnAdd) return Result.Failure(DataError.Local.UNKNOWN)
        val id = nextId++
        _turns.add(turn.copy(id = id))
        return Result.Success(id)
    }

    override suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local> {
        if (shouldFailOnUpdate) return Result.Failure(DataError.Local.UNKNOWN)
        val index = _turns.indexOfFirst { it.id == turn.id }
        if (index == -1) return Result.Failure(DataError.Local.NOT_FOUND)
        _turns[index] = turn
        return Result.Success(Unit)
    }
    override suspend fun getTurnsForGame(gameId: Long): Result<List<Turn>, DataError.Local> {
        if (shouldFailOnGetAll) return Result.Failure(DataError.Local.UNKNOWN)
        return Result.Success(_turns.toList())
    }
}