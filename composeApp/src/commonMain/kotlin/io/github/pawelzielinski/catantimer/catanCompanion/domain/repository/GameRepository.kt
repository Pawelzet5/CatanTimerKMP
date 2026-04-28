package io.github.pawelzielinski.catantimer.catanCompanion.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

interface GameRepository {
    /** Emits all games, sorted by most recently started. */
    fun getAllGames(): Flow<List<Game>>

    /** Emits only in-progress games, sorted by most recently started. */
    fun getInProgressGames(): Flow<List<Game>>

    /** Emits only completed games, sorted by most recently finished. */
    fun getCompletedGames(): Flow<List<Game>>

    /** Emits the game with the given [id], or null if it does not exist. */
    fun getGameById(id: Long): Flow<Game?>

    /** Emits the most recently started in-progress game, or null if none exists. */
    fun getMostRecentInProgressGame(): Flow<Game?>

    /**
     * Creates a new in-progress game with the given settings and player order.
     * Returns the new game's id on success.
     */
    suspend fun createGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long, DataError.Local>

    /** Updates expansion and special-turn-rule settings for an existing game. */
    suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<DataError.Local>

    /** Marks a game as completed, recording the optional winner. */
    suspend fun endGame(gameId: Long, winnerId: Long?): EmptyResult<DataError.Local>

    /** Permanently deletes the game with the given [id]. */
    suspend fun deleteGame(id: Long): EmptyResult<DataError.Local>
}
