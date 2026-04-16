package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
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

    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAll().map { entities ->
            entities.map { entity ->
                val gamesPlayed = playerDao.getGameCount(entity.id)
                entity.toDomain(gamesPlayed = gamesPlayed)
            }
        }

    override fun getVisiblePlayers(): Flow<List<Player>> =
        playerDao.getVisible().map { entities ->
            entities.map { entity ->
                val gamesPlayed = playerDao.getGameCount(entity.id)
                entity.toDomain(gamesPlayed = gamesPlayed)
            }
        }

    override fun getPlayerById(id: Long): Flow<Player?> =
        playerDao.getById(id).map { entity -> entity?.toDomain() }

    override suspend fun createPlayer(name: String): Result<Long, DataError.Local> =
        tryLocalWrite {
            val id = playerDao.insert(PlayerEntity(name = name, createdAt = System.currentTimeMillis()))
            Result.Success(id)
        }

    override suspend fun updatePlayer(player: Player): EmptyResult<DataError.Local> =
        tryLocalWrite {
            playerDao.update(player.toEntity())
            Result.Success(Unit)
        }

    override suspend fun hidePlayer(id: Long): EmptyResult<DataError.Local> =
        tryLocalWrite {
            playerDao.hide(id)
            Result.Success(Unit)
        }

    override suspend fun deletePlayer(id: Long): EmptyResult<DataError.Local> =
        tryLocalWrite {
            // Room's @Delete uses the primary key only, so other fields are irrelevant
            playerDao.delete(PlayerEntity(id = id, name = ""))
            Result.Success(Unit)
        }

    override suspend fun canDeletePlayer(id: Long): Boolean =
        playerDao.getGameCount(id) == 0
}
