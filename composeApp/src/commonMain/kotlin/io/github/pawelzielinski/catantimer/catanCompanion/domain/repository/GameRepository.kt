package io.github.pawelzielinski.catantimer.catanCompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

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
    ): Result<Long, DataError.Local>
    suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<DataError.Local>
    suspend fun endGame(gameId: Long, winnerId: Long?): EmptyResult<DataError.Local>
    suspend fun deleteGame(id: Long): EmptyResult<DataError.Local>
}
