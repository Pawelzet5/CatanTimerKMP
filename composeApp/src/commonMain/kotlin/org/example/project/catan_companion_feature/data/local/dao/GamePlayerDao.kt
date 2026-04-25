package io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.GamePlayerEntity

@Dao
interface GamePlayerDao {
    @Query("SELECT * FROM game_players WHERE gameId = :gameId ORDER BY orderIndex ASC")
    suspend fun getForGame(gameId: Long): List<GamePlayerEntity>

    @Insert
    suspend fun insertAll(players: List<GamePlayerEntity>)

    @Query("DELETE FROM game_players WHERE gameId = :gameId")
    suspend fun deleteForGame(gameId: Long)
}
