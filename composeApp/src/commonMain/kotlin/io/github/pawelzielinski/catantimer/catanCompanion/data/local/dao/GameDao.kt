package io.github.pawelzielinski.catantimer.catanCompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GameEntity

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY startedAt DESC")
    fun getAll(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE status = 'IN_PROGRESS' ORDER BY startedAt DESC")
    fun getInProgress(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE status = 'COMPLETED' ORDER BY finishedAt DESC")
    fun getCompleted(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun getById(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE status = 'IN_PROGRESS' ORDER BY startedAt DESC LIMIT 1")
    fun getMostRecentInProgress(): Flow<GameEntity?>

    @Insert
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Delete
    suspend fun delete(game: GameEntity)
}
