package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.catan_companion_feature.data.local.dao.GameDao
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerCrossRefEntity
import org.example.project.catan_companion_feature.data.local.mapper.toDomain
import org.example.project.catan_companion_feature.data.local.mapper.toGameEntity
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameSummary
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.core.data.tryLocalRead
import org.example.project.core.data.tryLocalWrite
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao
) : GameRepository {

    override suspend fun addGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        players: List<Player>,
        startedAt: Long
    ): Result<Long, DataError.Local> = tryLocalWrite {
        val gameId = gameDao.insertGame(
            toGameEntity(
                turnDurationMillis = turnDurationMillis,
                expansions = expansions,
                specialTurnRuleEnabled = specialTurnRuleEnabled,
                startedAt = startedAt
            )
        )
        val crossRefs = buildGamePlayerCrossRefs(gameId, players)
        gameDao.insertGamePlayerCrossRefs(crossRefs)
        Result.Success(gameId)
    }

    override suspend fun getGame(gameId: Long): Result<Game, DataError.Local> =
        tryLocalRead {
            val gameEntity = gameDao.getGame(gameId)
                ?: return@tryLocalRead Result.Failure(DataError.Local.NOT_FOUND)
            val orderedPlayers = playerDao.getPlayersForGame(gameId).map { it.toDomain() }
            Result.Success(gameEntity.toDomain(orderedPlayers))
        }

    override fun getGameSummaries(): Flow<List<GameSummary>> =
        gameDao.getGameSummaries()
            .map { projections -> projections.map { it.toDomain() } }

    override suspend fun getActiveGame(): Result<Game, DataError.Local> {
        // TODO: implement session resume logic (24h threshold, multiple active games handling)
        return Result.Failure(DataError.Local.NOT_FOUND)
    }

    override suspend fun saveGameAsFinished(gameId: Long, finishedAt: Long): EmptyResult<DataError.Local> =
        tryLocalWrite {
            val updated = gameDao.updateGameStatusToFinished(gameId, finishedAt)
            if (updated == 0) return@tryLocalWrite Result.Failure(DataError.Local.NOT_FOUND)
            Result.Success(Unit)
        }

    private fun buildGamePlayerCrossRefs(gameId: Long, players: List<Player>): List<GamePlayerCrossRefEntity> =
        players.mapIndexed { index, player ->
            GamePlayerCrossRefEntity(
                gameId = gameId,
                playerId = player.id,
                playerIndex = index
            )
        }
}
