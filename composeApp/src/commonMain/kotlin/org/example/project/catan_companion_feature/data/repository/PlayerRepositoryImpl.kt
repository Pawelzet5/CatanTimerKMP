package org.example.project.catan_companion_feature.data.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class PlayerRepositoryImpl(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override fun getAllPlayers(): Flow<List<Player>> = TODO("Implemented in session-3/repository-impls")

    override fun getVisiblePlayers(): Flow<List<Player>> = TODO("Implemented in session-3/repository-impls")

    override fun getPlayerById(id: Long): Flow<Player?> = TODO("Implemented in session-3/repository-impls")

    override suspend fun createPlayer(name: String): Result<Long, DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun updatePlayer(player: Player): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun hidePlayer(id: Long): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun deletePlayer(id: Long): EmptyResult<DataError.Local> = TODO("Implemented in session-3/repository-impls")

    override suspend fun canDeletePlayer(id: Long): Boolean = TODO("Implemented in session-3/repository-impls")
}
