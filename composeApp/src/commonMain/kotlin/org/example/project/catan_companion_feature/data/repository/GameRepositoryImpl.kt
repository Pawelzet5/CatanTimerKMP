package org.example.project.catan_companion_feature.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.example.project.catan_companion_feature.data.local.CatanCompanionDatabase
import org.example.project.catan_companion_feature.data.local.dao.GameDao
import org.example.project.catan_companion_feature.data.local.dao.GamePlayerDao
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerEntity
import org.example.project.catan_companion_feature.data.local.mapper.toDomain
import org.example.project.catan_companion_feature.data.local.mapper.toEntity
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.core.data.tryLocalRead
import org.example.project.core.data.tryLocalWrite
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class GameRepositoryImpl(
    private val database: CatanCompanionDatabase,
    private val gameDao: GameDao,
    private val gamePlayerDao: GamePlayerDao,
    private val playerDao: PlayerDao
) : GameRepository {

    override fun getAllGames(): Flow<List<Game>> =
        gameDao.getAll().map { entities -> entities.map { it.toDomainWithPlayers() } }

    override fun getInProgressGames(): Flow<List<Game>> =
        gameDao.getInProgress().map { entities -> entities.map { it.toDomainWithPlayers() } }

    override fun getCompletedGames(): Flow<List<Game>> =
        gameDao.getCompleted().map { entities -> entities.map { it.toDomainWithPlayers() } }

    override fun getGameById(id: Long): Flow<Game?> =
        gameDao.getById(id).map { entity -> entity?.toDomainWithPlayers() }

    override fun getMostRecentInProgressGame(): Flow<Game?> =
        gameDao.getMostRecentInProgress().map { entity -> entity?.toDomainWithPlayers() }

    override suspend fun createGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long, DataError.Local> = tryLocalWrite {
        val gameId = database.withTransaction {
            val id = gameDao.insert(
                GameEntity(
                    turnDurationMillis = turnDurationMillis,
                    expansions = expansions,
                    specialTurnRuleEnabled = specialTurnRuleEnabled,
                    status = GameStatus.IN_PROGRESS,
                    startedAt = System.currentTimeMillis()
                )
            )
            gamePlayerDao.insertAll(
                playerIds.mapIndexed { index, playerId ->
                    GamePlayerEntity(gameId = id, playerId = playerId, orderIndex = index)
                }
            )
            id
        }
        Result.Success(gameId)
    }

    override suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<DataError.Local> = tryLocalWrite {
        val entity = gameDao.getById(gameId).first()
            ?: return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
        gameDao.update(entity.copy(expansions = expansions, specialTurnRuleEnabled = specialTurnRuleEnabled))
        Result.Success(Unit)
    }

    override suspend fun endGame(gameId: Long, winnerId: Long?): EmptyResult<DataError.Local> =
        tryLocalWrite {
            val entity = gameDao.getById(gameId).first()
                ?: return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
            gameDao.update(
                entity.copy(
                    status = GameStatus.COMPLETED,
                    finishedAt = System.currentTimeMillis(),
                    winnerId = winnerId
                )
            )
            Result.Success(Unit)
        }

    override suspend fun deleteGame(id: Long): EmptyResult<DataError.Local> = tryLocalWrite {
        val entity = gameDao.getById(id).first()
            ?: return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
        gameDao.delete(entity)
        Result.Success(Unit)
    }

    private suspend fun GameEntity.toDomainWithPlayers(): Game {
        val gamePlayers = gamePlayerDao.getForGame(id).map { gpEntity ->
            val playerName = playerDao.getById(gpEntity.playerId).first()?.name ?: ""
            gpEntity.toDomain(playerName = playerName)
        }
        return toDomain(gamePlayers)
    }
}
