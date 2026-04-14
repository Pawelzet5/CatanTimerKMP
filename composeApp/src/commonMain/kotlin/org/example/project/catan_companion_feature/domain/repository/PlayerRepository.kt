package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    fun getVisiblePlayers(): Flow<List<Player>>
    fun getPlayerById(id: Long): Flow<Player?>
    suspend fun createPlayer(name: String): Result<Long, DataError.Local>
    suspend fun updatePlayer(player: Player): EmptyResult<DataError.Local>
    suspend fun hidePlayer(id: Long): EmptyResult<DataError.Local>
    suspend fun deletePlayer(id: Long): EmptyResult<DataError.Local>
    suspend fun canDeletePlayer(id: Long): Boolean
}
