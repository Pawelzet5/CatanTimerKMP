package io.github.pawelzielinski.catantimer.catanCompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

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
