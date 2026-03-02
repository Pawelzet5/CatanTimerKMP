package org.example.project.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerCrossRefEntity
import org.example.project.catan_companion_feature.data.local.entity.GameWithPlayers

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGamePlayerCrossRefs(crossRefs: List<GamePlayerCrossRefEntity>)

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameWithPlayers(gameId: Long): GameWithPlayers?

    @Transaction
    @Query("SELECT * FROM games WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveGameWithPlayers(): GameWithPlayers?

    @Transaction
    @Query("SELECT * FROM games ORDER BY id DESC")
    suspend fun getAllGamesWithPlayers(): List<GameWithPlayers>

    @Query("UPDATE games SET status = 'FINISHED' WHERE id = :gameId")
    suspend fun updateGameStatusToFinished(gameId: Long): Int  // returns number of rows affected
}