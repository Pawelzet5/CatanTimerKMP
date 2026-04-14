package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameSummary
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

interface GameRepository {
    suspend fun addGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        players: List<Player>,
        startedAt: Long
    ): Result<Long, DataError.Local>
    suspend fun getGame(gameId: Long): Result<Game, DataError.Local>
    fun getGameSummaries(): Flow<List<GameSummary>>
    suspend fun getActiveGame(): Result<Game, DataError.Local>
    suspend fun saveGameAsFinished(gameId: Long, finishedAt: Long): EmptyResult<DataError.Local>
}
