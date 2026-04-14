package org.example.project.catan_companion_feature.data.fakes.dao

import org.example.project.catan_companion_feature.data.local.dao.TurnDao
import org.example.project.catan_companion_feature.data.local.entity.TurnEntity

class FakeTurnDao : TurnDao {

    private val _turns = mutableListOf<TurnEntity>()

    // region state controls

    /** Overrides the number of rows reported as updated. -1 means "use real count". */
    var updatedRowCount: Int = -1

    var lastInsertedId: Long = 0L
        private set

    private var nextId = 1L

    // endregion

    // region TurnDao impl

    override suspend fun insertTurn(turn: TurnEntity): Long {
        val id = nextId++
        _turns.add(turn.copy(id = id))
        lastInsertedId = id
        return id
    }

    override suspend fun updateTurn(turn: TurnEntity): Int {
        if (updatedRowCount != -1) return updatedRowCount

        val index = _turns.indexOfFirst { it.id == turn.id }
        if (index == -1) return 0
        _turns[index] = turn
        return 1
    }

    override suspend fun getLastTurn(gameId: Long): TurnEntity? =
        _turns
            .filter { it.gameId == gameId }
            .maxByOrNull { it.number }

    override suspend fun getTurnsForGame(gameId: Long): List<TurnEntity> =
        _turns
            .filter { it.gameId == gameId }
            .sortedBy { it.number }

    // endregion
}