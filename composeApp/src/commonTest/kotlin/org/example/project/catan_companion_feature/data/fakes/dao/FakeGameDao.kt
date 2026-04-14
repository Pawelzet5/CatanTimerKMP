package org.example.project.catan_companion_feature.data.fakes.dao

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
        val id = if (game.id != 0L) game.id else nextId++
        val stored = game.copy(id = id)
        _games[id] = stored
        lastInsertedGameId = id
        rebuildSummariesFlow()
        return id
    }

    override suspend fun insertGamePlayerCrossRefs(crossRefs: List<GamePlayerCrossRefEntity>) {
        _crossRefs.addAll(crossRefs)
        rebuildSummariesFlow()
    }

    override suspend fun getGame(gameId: Long): GameEntity? =
        _games[gameId]

    override fun getGameSummaries(): Flow<List<GameSummaryProjection>> =
        _summariesFlow

    override suspend fun updateGameStatusToFinished(gameId: Long, finishedAt: Long): Int {
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
     * turnCount is intentionally 0 — TurnDao owns that data, not GameDao.
     * Tests that need accurate turnCount should use integration tests against real Room.
     */
    private fun rebuildSummariesFlow() {
        _summariesFlow.value = _games.values
            .sortedByDescending { it.id }
            .map { game ->
                GameSummaryProjection(
                    id = game.id,
                    status = game.status,
                    playerCount = _crossRefs.count { it.gameId == game.id },
                    turnCount = 0,
                    startedAt = game.startedAt,
                    finishedAt = game.finishedAt
                )
            }
    }

    // endregion
}