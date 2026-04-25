package io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.TurnEntity

@Dao
interface TurnDao {
    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number ASC")
    fun getForGame(gameId: Long): Flow<List<TurnEntity>>

    @Query("SELECT * FROM turns WHERE id = :id")
    fun getById(id: Long): Flow<TurnEntity?>

    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number DESC LIMIT 1")
    fun getCurrentForGame(gameId: Long): Flow<TurnEntity?>

    @Insert
    suspend fun insert(turn: TurnEntity): Long

    @Update
    suspend fun update(turn: TurnEntity)
}
