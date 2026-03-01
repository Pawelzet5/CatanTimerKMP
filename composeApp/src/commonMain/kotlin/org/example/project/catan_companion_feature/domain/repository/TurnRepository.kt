package org.example.project.catan_companion_feature.domain.repository

import org.example.project.core.util.Result
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.core.util.DataError
import org.example.project.core.util.EmptyResult

interface TurnRepository {
    suspend fun addTurn(turn: Turn): Result<Long, DataError.Local>
    suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local>
    suspend fun getTurnsForGame(gameId: Long): Result<List<Turn>, DataError.Local>
}
