package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Player

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    fun getVisiblePlayers(): Flow<List<Player>>
    fun getPlayerById(id: Long): Flow<Player?>
    suspend fun createPlayer(name: String): Long
    suspend fun updatePlayer(player: Player)
    suspend fun hidePlayer(id: Long)
    suspend fun deletePlayer(id: Long)
    suspend fun canDeletePlayer(id: Long): Boolean
}
