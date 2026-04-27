package io.github.pawelzielinski.catantimer.catanCompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

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
