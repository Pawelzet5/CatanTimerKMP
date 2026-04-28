package io.github.pawelzielinski.catantimer.catanCompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

interface PlayerRepository {
    /** Emits all players, including hidden ones, sorted by name. */
    fun getAllPlayers(): Flow<List<Player>>

    /** Emits only non-hidden players, sorted by name. */
    fun getVisiblePlayers(): Flow<List<Player>>

    /** Emits the player with the given [id], or null if they do not exist. */
    fun getPlayerById(id: Long): Flow<Player?>

    /** Creates a new player with the given [name]. Returns the new player's id on success. */
    suspend fun createPlayer(name: String): Result<Long, DataError.Local>

    /** Persists all fields of the given [player]. */
    suspend fun updatePlayer(player: Player): EmptyResult<DataError.Local>

    /** Marks the player as hidden so they no longer appear in selection lists. */
    suspend fun hidePlayer(id: Long): EmptyResult<DataError.Local>

    /** Permanently deletes the player with the given [id]. */
    suspend fun deletePlayer(id: Long): EmptyResult<DataError.Local>

    /** Returns true if the player has no associated games and can be safely deleted. */
    suspend fun canDeletePlayer(id: Long): Boolean
}
