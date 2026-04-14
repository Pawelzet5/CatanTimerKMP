package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

interface TurnRepository {
    fun getTurnsForGame(gameId: Long): Flow<List<Turn>>
    fun getTurnById(id: Long): Flow<Turn?>
    fun getCurrentTurn(gameId: Long): Flow<Turn?>
    suspend fun createTurn(gameId: Long, playerId: Long, number: Int): Result<Long, DataError.Local>
    suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local>
    suspend fun updateDiceRoll(turnId: Long, redDice: Int, yellowDice: Int, eventDice: EventDiceType?): EmptyResult<DataError.Local>
    suspend fun updateDuration(turnId: Long, durationMillis: Long): EmptyResult<DataError.Local>
    suspend fun setSecondaryPlayer(turnId: Long, playerId: Long): EmptyResult<DataError.Local>
}
