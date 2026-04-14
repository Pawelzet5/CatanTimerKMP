package org.example.project.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity

@Dao
interface PlayerDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayer(playerId: Long): PlayerEntity?

    @Query("SELECT * FROM players ORDER BY name ASC")
    suspend fun getAllPlayers(): List<PlayerEntity>

    // Search by name (case-insensitive, LIKE)
    @Query("SELECT * FROM players WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchPlayers(query: String): Flow<List<PlayerEntity>>

    // Players assigned to the given game; ORDER BY playerIndex guarantees Game.players order
    @Query("""
        SELECT p.* FROM players p
        INNER JOIN game_player_cross_ref ref ON p.id = ref.playerId
        WHERE ref.gameId = :gameId
        ORDER BY ref.playerIndex ASC
    """)
    suspend fun getPlayersForGame(gameId: Long): List<PlayerEntity>

    // Number of games the player has participated in
    @Query("SELECT COUNT(*) FROM game_player_cross_ref WHERE playerId = :playerId")
    suspend fun getGameCountForPlayer(playerId: Long): Int

    // Deletes the player – Room will throw an exception if the player has a RESTRICT FK in turns or cross_ref
    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: Long)
}