package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.enums.GameExpansion

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>
    fun getInProgressGames(): Flow<List<Game>>
    fun getCompletedGames(): Flow<List<Game>>
    fun getGameById(id: Long): Flow<Game?>
    fun getMostRecentInProgressGame(): Flow<Game?>
    suspend fun createGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Long
    suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    )
    suspend fun endGame(gameId: Long, winnerId: Long?)
    suspend fun deleteGame(id: Long)
}
