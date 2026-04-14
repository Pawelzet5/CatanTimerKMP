package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.data.local.dao.GameDao
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao
) : GameRepository {

    override fun getAllGames(): Flow<List<Game>> = TODO("Implemented in session-3/repository-impls")

    override fun getInProgressGames(): Flow<List<Game>> = TODO("Implemented in session-3/repository-impls")

    override fun getCompletedGames(): Flow<List<Game>> = TODO("Implemented in session-3/repository-impls")

    override fun getGameById(id: Long): Flow<Game?> = TODO("Implemented in session-3/repository-impls")

    override fun getMostRecentInProgressGame(): Flow<Game?> = TODO("Implemented in session-3/repository-impls")

    override suspend fun createGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long, DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun endGame(gameId: Long, winnerId: Long?): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun deleteGame(id: Long): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")
}
