package org.example.project.catan_companion_feature.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.core.domain.*

interface PlayerRepository {

    suspend fun addPlayer(player: Player): Result<Long, DataError.Local>

    suspend fun getPlayer(playerId: Long): Result<Player, DataError.Local>

    fun getPlayers(query: String): Flow<List<Player>>

    suspend fun getPlayersForGame(gameId: Long): Result<List<Player>, DataError.Local>

    suspend fun removePlayer(playerId: Long): EmptyResult<DataError.Local>
}