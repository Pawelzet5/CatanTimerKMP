package org.example.project.catan_companion_feature.data.fakes.dao

import androidx.sqlite.SQLiteException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.catan_companion_feature.data.local.dao.GameDao
import org.example.project.catan_companion_feature.data.local.entity.*
import org.example.project.catan_companion_feature.domain.enums.GameStatus

class FakeGameDao : GameDao {

    private val _games = mutableMapOf<Long, GameEntity>()
    private val _crossRefs = mutableListOf<GamePlayerCrossRefEntity>()

    // Backing flow for getGameSummaries — updated on every insert / status change
    private val _summariesFlow = MutableStateFlow<List<GameSummaryProjection>>(emptyList())

    // region error flags

    var shouldThrowSQLiteExceptionOnInsert = false
    var shouldThrowUnexpectedExceptionOnInsert = false

    var shouldThrowSQLiteExceptionOnCrossRefInsert = false

    var shouldThrowSQLiteExceptionOnRead = false
    var shouldThrowUnexpectedExceptionOnRead = false

    var shouldThrowSQLiteExceptionOnStatusUpdate = false
    var shouldThrowUnexpectedExceptionOnStatusUpdate = false

    // endregion

    // region state controls

    var lastInsertedGameId: Long = 0L
        private set

    /** Overrides rows-affected count for updateGameStatusToFinished. -1 means "use real count". */
    var updateStatusRowCount: Int = -1

    private var nextId = 1L

    /** Exposes cross-refs for assertion in tests. */
    fun getCrossRefsForGame(gameId: Long): List<GamePlayerCrossRefEntity> =
        _crossRefs.filter { it.gameId == gameId }

    // endregion

    // region GameDao impl

    override suspend fun insertGame(game: GameEntity): Long {
        if (shouldThrowSQLiteExceptionOnInsert) throw SQLiteException("Fake: disk full")
        if (shouldThrowUnexpectedExceptionOnInsert) throw RuntimeException("Fake: unexpected error")

        val id = if (game.id != 0L) game.id else nextId++
        val stored = game.copy(id = id)
        _games[id] = stored
        lastInsertedGameId = id
        rebuildSummariesFlow()
        return id
    }

    override suspend fun insertGamePlayerCrossRefs(crossRefs: List<GamePlayerCrossRefEntity>) {
        if (shouldThrowSQLiteExceptionOnCrossRefInsert) throw SQLiteException("Fake: disk full on cross-ref")

        _crossRefs.addAll(crossRefs)
        rebuildSummariesFlow()
    }

    override suspend fun getGame(gameId: Long): GameEntity? {
        if (shouldThrowSQLiteExceptionOnRead) throw SQLiteException("Fake: read error")
        if (shouldThrowUnexpectedExceptionOnRead) throw RuntimeException("Fake: unexpected error")

        return _games[gameId]
    }

    override fun getGameSummaries(): Flow<List<GameSummaryProjection>> = _summariesFlow

    override suspend fun updateGameStatusToFinished(gameId: Long, finishedAt: Long): Int {
        if (shouldThrowSQLiteExceptionOnStatusUpdate) throw SQLiteException("Fake: disk full")
        if (shouldThrowUnexpectedExceptionOnStatusUpdate) throw RuntimeException("Fake: unexpected error")

        if (updateStatusRowCount != -1) return updateStatusRowCount

        val game = _games[gameId] ?: return 0
        _games[gameId] = game.copy(status = GameStatus.FINISHED, finishedAt = finishedAt)
        rebuildSummariesFlow()
        return 1
    }

    // endregion

    // region helpers

    /**
     * Recomputes the summaries flow to mirror the GROUP BY query in the real DAO.
     * playerCount and turnCount are intentionally left as 0 here — tests that need
     * accurate counts should assert on the repository layer, not on the projection directly.
     */
    private fun rebuildSummariesFlow() {
        _summariesFlow.value = _games.values
            .sortedByDescending { it.id }
            .map { game ->
                val playerCount = _crossRefs.count { it.gameId == game.id }
                GameSummaryProjection(
                    id = game.id,
                    status = game.status,
                    playerCount = playerCount,
                    turnCount = 0,
                    startedAt = game.startedAt,
                    finishedAt = game.finishedAt
                )
            }
    }

    // endregion
}