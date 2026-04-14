package org.example.project.catan_companion_feature.data.fakes.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.example.project.catan_companion_feature.data.local.dao.TurnDao
import org.example.project.catan_companion_feature.data.local.entity.TurnEntity

class FakeTurnDao : TurnDao {

    private val _turns = mutableMapOf<Long, TurnEntity>()
    private val _turnsState = MutableStateFlow<List<TurnEntity>>(emptyList())

    // region state controls

    var lastInsertedId: Long = 0L
        private set

    private var nextId = 1L

    // endregion

    // region TurnDao impl

    override fun getForGame(gameId: Long): Flow<List<TurnEntity>> =
        _turnsState.map { turns ->
            turns.filter { it.gameId == gameId }.sortedBy(TurnEntity::number)
        }

    override fun getById(id: Long): Flow<TurnEntity?> =
        _turnsState.map { turns -> turns.find { it.id == id } }

    override fun getCurrentForGame(gameId: Long): Flow<TurnEntity?> =
        _turnsState.map { turns ->
            turns.filter { it.gameId == gameId }.maxByOrNull(TurnEntity::number)
        }

    override suspend fun insert(turn: TurnEntity): Long {
        val id = nextId++
        val stored = turn.copy(id = id)
        _turns[id] = stored
        lastInsertedId = id
        _turnsState.value = _turns.values.toList()
        return id
    }

    override suspend fun update(turn: TurnEntity) {
        if (_turns.containsKey(turn.id)) {
            _turns[turn.id] = turn
            _turnsState.value = _turns.values.toList()
        }
    }

    // endregion
}
