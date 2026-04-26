package io.github.pawelzielinski.catantimer.catan_companion_feature.data.fakes.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.repository.TurnRepository
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

class FakeTurnRepository : TurnRepository {

    // turnId -> Turn (current state)
    private val _turns = mutableMapOf<Long, Turn>()
    // gameId -> ordered list of turn IDs (insertion order)
    private val _gameToTurnIds = mutableMapOf<Long, MutableList<Long>>()

    val allTurns: List<Turn>
        get() = _gameToTurnIds.values.flatten().mapNotNull { _turns[it] }

    var shouldFailOnAdd = false
    var shouldFailOnUpdate = false
    var shouldFailOnGetAll = false

    private var nextId = 1L

    /**
     * Prepopulates the repository with a list of turns, grouped by their gameId.
     */
    fun setTurns(turns: List<Turn>) {
        turns.forEach { seedTurns(it.gameId, it) }
    }

    /**
     * Prepopulates the repository with existing turns for test setup.
     * IDs are taken from the Turn objects — [nextId] is advanced to avoid future collisions.
     */
    fun seedTurns(gameId: Long, vararg turns: Turn) {
        val bucket = _gameToTurnIds.getOrPut(gameId) { mutableListOf() }
        turns.forEach { turn ->
            _turns[turn.id] = turn
            bucket.add(turn.id)
        }
        val maxId = _turns.keys.maxOrNull() ?: 0L
        nextId = maxId + 1L
    }

    override fun getTurnsForGame(gameId: Long): Flow<List<Turn>> = flow {
        if (shouldFailOnGetAll) throw Exception("Fake: get turns failure")
        val ids = _gameToTurnIds[gameId] ?: emptyList()
        emit(ids.mapNotNull { _turns[it] })
    }

    override fun getTurnById(id: Long): Flow<Turn?> = flow {
        emit(_turns[id])
    }

    override fun getCurrentTurn(gameId: Long): Flow<Turn?> = flow {
        val ids = _gameToTurnIds[gameId] ?: emptyList()
        emit(ids.mapNotNull { _turns[it] }.maxByOrNull { it.number })
    }

    override suspend fun createTurn(
        gameId: Long,
        playerId: Long,
        number: Int
    ): Result<Long, DataError.Local> {
        if (shouldFailOnAdd) return Result.Failure(DataError.Local.UNKNOWN)
        val id = nextId++
        val turn = Turn(id = id, gameId = gameId, number = number, playerId = playerId, playerName = "")
        _turns[id] = turn
        _gameToTurnIds.getOrPut(gameId) { mutableListOf() }.add(id)
        return Result.Success(id)
    }

    override suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local> {
        if (shouldFailOnUpdate) return Result.Failure(DataError.Local.UNKNOWN)
        if (!_turns.containsKey(turn.id)) return Result.Failure(DataError.Local.NOT_FOUND)
        _turns[turn.id] = turn
        return Result.Success(Unit)
    }

    override suspend fun updateDiceRoll(
        turnId: Long,
        redDice: Int,
        yellowDice: Int,
        eventDice: EventDiceType?
    ): EmptyResult<DataError.Local> {
        if (shouldFailOnUpdate) return Result.Failure(DataError.Local.UNKNOWN)
        val turn = _turns[turnId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _turns[turnId] = turn.copy(redDice = redDice, yellowDice = yellowDice, eventDice = eventDice)
        return Result.Success(Unit)
    }

    override suspend fun updateDuration(
        turnId: Long,
        durationMillis: Long
    ): EmptyResult<DataError.Local> {
        if (shouldFailOnUpdate) return Result.Failure(DataError.Local.UNKNOWN)
        val turn = _turns[turnId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _turns[turnId] = turn.copy(durationMillis = durationMillis)
        return Result.Success(Unit)
    }

    override suspend fun setSecondaryPlayer(
        turnId: Long,
        playerId: Long
    ): EmptyResult<DataError.Local> {
        val turn = _turns[turnId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _turns[turnId] = turn.copy(secondaryPlayerId = playerId)
        return Result.Success(Unit)
    }
}
