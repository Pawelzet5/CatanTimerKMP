package org.example.project.catan_companion_feature.domain.repository

import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.core.util.DataError
import org.example.project.core.util.EmptyResult
import org.example.project.core.util.Result

interface GameRepository {
    suspend fun addGame(config: GameConfig): Result<Long, DataError.Local>
    suspend fun getGame(gameId: Long): Result<Game, DataError.Local>
    suspend fun getActiveGame(): Result<Game, DataError.Local>
    suspend fun getGames(): Result<List<Game>, DataError.Local>
    suspend fun saveGameAsFinished(gameId: Long): EmptyResult<DataError.Local>
}