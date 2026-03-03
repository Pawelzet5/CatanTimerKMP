package org.example.project.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerCrossRefEntity
import kotlinx.coroutines.flow.Flow
import org.example.project.catan_companion_feature.data.local.entity.GameSummaryProjection

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGamePlayerCrossRefs(crossRefs: List<GamePlayerCrossRefEntity>)

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGame(gameId: Long): GameEntity?

    @Query("""
        SELECT g.id, g.status, g.startedAt, g.finishedAt,
               COUNT(DISTINCT ref.playerId) AS playerCount,
               COUNT(DISTINCT t.id)         AS turnCount
        FROM games g
        LEFT JOIN game_player_cross_ref ref ON ref.gameId = g.id
        LEFT JOIN turns t                   ON t.gameId   = g.id
        GROUP BY g.id
        ORDER BY g.id DESC
    """)
    fun getGameSummaries(): Flow<List<GameSummaryProjection>>  // bez suspend

    @Query("UPDATE games SET status = 'FINISHED', finishedAt = :finishedAt WHERE id = :gameId")
    suspend fun updateGameStatusToFinished(gameId: Long, finishedAt: Long): Int
}