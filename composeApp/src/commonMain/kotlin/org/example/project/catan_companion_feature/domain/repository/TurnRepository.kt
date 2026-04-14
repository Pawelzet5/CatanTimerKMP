package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.EventDiceType

interface TurnRepository {
    fun getTurnsForGame(gameId: Long): Flow<List<Turn>>
    fun getTurnById(id: Long): Flow<Turn?>
    fun getCurrentTurn(gameId: Long): Flow<Turn?>
    suspend fun createTurn(gameId: Long, playerId: Long, number: Int): Long
    suspend fun updateTurn(turn: Turn)
    suspend fun updateDiceRoll(turnId: Long, redDice: Int, yellowDice: Int, eventDice: EventDiceType?)
    suspend fun updateDuration(turnId: Long, durationMillis: Long)
    suspend fun setSecondaryPlayer(turnId: Long, playerId: Long)
}
