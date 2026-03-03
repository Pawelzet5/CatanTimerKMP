package org.example.project.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.example.project.catan_companion_feature.data.local.entity.TurnEntity

@Dao
interface TurnDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTurn(turn: TurnEntity): Long

    @Update
    suspend fun updateTurn(turn: TurnEntity): Int  // returns number of rows affected

    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number DESC LIMIT 1")
    suspend fun getLastTurn(gameId: Long): TurnEntity?

    @Query("SELECT * FROM turns WHERE gameId = :gameId ORDER BY number ASC")
    suspend fun getTurnsForGame(gameId: Long): List<TurnEntity>
}