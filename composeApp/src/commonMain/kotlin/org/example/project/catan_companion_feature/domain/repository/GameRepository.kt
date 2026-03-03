package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.*
import org.example.project.core.domain.*

interface GameRepository {
    suspend fun addGame(config: GameConfig, startedAt: Long): Result<Long, DataError.Local>
    suspend fun getGame(gameId: Long): Result<Game, DataError.Local>
    fun getGameSummaries(): Flow<List<GameSummary>>
    suspend fun getActiveGame(): Result<Game, DataError.Local>
    suspend fun saveGameAsFinished(gameId: Long, finishedAt: Long): EmptyResult<DataError.Local>
}