package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.data.local.mapper.toDomain
import org.example.project.catan_companion_feature.data.local.mapper.toEntity
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository
import org.example.project.core.data.tryLocalRead
import org.example.project.core.data.tryLocalWrite
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class PlayerRepositoryImpl(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override suspend fun addPlayer(player: Player): Result<Long, DataError.Local> =
        tryLocalWrite {
            Result.Success(playerDao.insertPlayer(player.toEntity()))
        }

    override suspend fun getPlayer(playerId: Long): Result<Player, DataError.Local> =
        tryLocalRead {
            playerDao.getPlayer(playerId)
                ?.let { Result.Success(it.toDomain()) }
                ?: Result.Failure(DataError.Local.NOT_FOUND)
        }

    override fun getPlayers(query: String): Flow<List<Player>> =
        playerDao.searchPlayers(query).map {
            playersList -> playersList.map { it.toDomain() }
        }


    override suspend fun getPlayersForGame(gameId: Long): Result<List<Player>, DataError.Local> =
        tryLocalRead {
            Result.Success(playerDao.getPlayersForGame(gameId).map { it.toDomain() })
        }

    override suspend fun removePlayer(playerId: Long): EmptyResult<DataError.Local> =
        tryLocalWrite {
            // If the player is linked to a turn or game, Room will throw SQLiteConstraintException
            // which will be caught by tryLocalWrite as UNKNOWN
            playerDao.deletePlayer(playerId)
            Result.Success(Unit)
        }
}