package org.example.project.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE isHidden = 0 ORDER BY name ASC")
    fun getVisible(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun getById(id: Long): Flow<PlayerEntity?>

    @Insert
    suspend fun insert(player: PlayerEntity): Long

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("UPDATE players SET isHidden = 1 WHERE id = :id")
    suspend fun hide(id: Long)

    @Delete
    suspend fun delete(player: PlayerEntity)

    @Query("SELECT COUNT(*) FROM game_players WHERE playerId = :playerId")
    suspend fun getGameCount(playerId: Long): Int
}
