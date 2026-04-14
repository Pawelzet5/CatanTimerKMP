package org.example.project.catan_companion_feature.data.fakes.repository

import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class FakeTurnRepository : TurnRepository {

    // gameId → ordered list of turns
    private val _turnsByGame = mutableMapOf<Long, MutableList<Turn>>()

    val allTurns: List<Turn> get() = _turnsByGame.values.flatten()

    var shouldFailOnAdd = false
    var shouldFailOnUpdate = false
    var shouldFailOnGetAll = false

    private var nextId = 1L

    /**
     * Prepopulates the repository with existing turns associated with [gameId]
     * (e.g. for session restore tests). IDs are set by the caller — [nextId]
     * is advanced to avoid future collisions.
     */
    fun seedTurns(gameId: Long, vararg turns: Turn) {
        val bucket = _turnsByGame.getOrPut(gameId) { mutableListOf() }
        bucket.addAll(turns)
        val maxId = _turnsByGame.values.flatten().maxOfOrNull { it.id } ?: 0L
        nextId = maxId + 1L
    }

    override suspend fun addTurn(gameId: Long, turn: Turn): Result<Long, DataError.Local> {
        if (shouldFailOnAdd) return Result.Failure(DataError.Local.UNKNOWN)
        val id = nextId++
        val bucket = _turnsByGame.getOrPut(gameId) { mutableListOf() }
        bucket.add(turn.copy(id = id))
        return Result.Success(id)
    }

    override suspend fun updateTurn(gameId: Long, turn: Turn): EmptyResult<DataError.Local> {
        if (shouldFailOnUpdate) return Result.Failure(DataError.Local.UNKNOWN)
        val bucket = _turnsByGame[gameId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        val index = bucket.indexOfFirst { it.id == turn.id }
        if (index == -1) return Result.Failure(DataError.Local.NOT_FOUND)
        bucket[index] = turn
        return Result.Success(Unit)
    }

    override suspend fun getTurnsForGame(gameId: Long): Result<List<Turn>, DataError.Local> {
        if (shouldFailOnGetAll) return Result.Failure(DataError.Local.UNKNOWN)
        return Result.Success(_turnsByGame[gameId]?.toList() ?: emptyList())
    }
}