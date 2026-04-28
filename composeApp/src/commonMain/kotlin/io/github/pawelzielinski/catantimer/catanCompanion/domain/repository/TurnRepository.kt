package io.github.pawelzielinski.catantimer.catanCompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

interface TurnRepository {
    /** Emits all turns for [gameId] in ascending turn-number order. */
    fun getTurnsForGame(gameId: Long): Flow<List<Turn>>

    /** Emits the turn with the given [id], or null if it does not exist. */
    fun getTurnById(id: Long): Flow<Turn?>

    /** Emits the latest turn for [gameId], or null if no turns exist for it. */
    fun getCurrentTurn(gameId: Long): Flow<Turn?>

    /** Creates a new turn for [gameId] and returns its id on success. */
    suspend fun createTurn(gameId: Long, playerId: Long, number: Int): Result<Long, DataError.Local>

    /** Persists all fields of the given [turn]. */
    suspend fun updateTurn(turn: Turn): EmptyResult<DataError.Local>

    /** Updates only the dice fields of the turn with the given [turnId]. */
    suspend fun updateDiceRoll(turnId: Long, redDice: Int, yellowDice: Int, eventDice: EventDiceType?): EmptyResult<DataError.Local>

    /** Updates only the duration of the turn with the given [turnId]. */
    suspend fun updateDuration(turnId: Long, durationMillis: Long): EmptyResult<DataError.Local>

    /** Assigns a secondary player to the turn with the given [turnId]. */
    suspend fun setSecondaryPlayer(turnId: Long, playerId: Long): EmptyResult<DataError.Local>
}
