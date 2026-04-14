package org.example.project.catan_companion_feature.data.fakes.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.catan_companion_feature.domain.dataclass.GameSummary
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class FakeGameRepository : GameRepository {

    private val _games = mutableMapOf<Long, Game>()
    val games: List<Game> get() = _games.values.toList()

    // Backing flow for getGameSummaries — updated on every mutation
    private val _summariesFlow = MutableStateFlow<List<GameSummary>>(emptyList())

    var shouldFailOnAddGame = false
    var shouldFailOnGetGame = false
    var shouldFailOnGetActiveGame = false
    var shouldFailOnFinishGame = false

    private var nextId = 1L

    /**
     * Prepopulates the repository with an existing [Game] (e.g. for use-case tests
     * that need a game to already exist without going through [addGame]).
     * Bypasses id-generation intentionally — id is set by the caller.
     */
    fun seedGame(game: Game) {
        _games[game.id] = game
        rebuildSummariesFlow()
    }

    override suspend fun addGame(config: GameConfig, startedAt: Long): Result<Long, DataError.Local> {
        if (shouldFailOnAddGame) return Result.Failure(DataError.Local.UNKNOWN)
        val id = nextId++
        val game = Game(
            id = id,
            config = config,
            status = GameStatus.ACTIVE,
            startedAt = startedAt
        )
        _games[id] = game
        rebuildSummariesFlow()
        return Result.Success(id)
    }

    override suspend fun getGame(gameId: Long): Result<Game, DataError.Local> {
        if (shouldFailOnGetGame) return Result.Failure(DataError.Local.UNKNOWN)
        return _games[gameId]
            ?.let { Result.Success(it) }
            ?: Result.Failure(DataError.Local.NOT_FOUND)
    }

    override fun getGameSummaries(): Flow<List<GameSummary>> = _summariesFlow.asStateFlow()

    override suspend fun getActiveGame(): Result<Game, DataError.Local> {
        if (shouldFailOnGetActiveGame) return Result.Failure(DataError.Local.UNKNOWN)
        return _games.values.firstOrNull { it.status == GameStatus.ACTIVE }
            ?.let { Result.Success(it) }
            ?: Result.Failure(DataError.Local.NOT_FOUND)
    }

    override suspend fun saveGameAsFinished(gameId: Long, finishedAt: Long): EmptyResult<DataError.Local> {
        if (shouldFailOnFinishGame) return Result.Failure(DataError.Local.UNKNOWN)
        val game = _games[gameId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _games[gameId] = game.copy(status = GameStatus.FINISHED, finishedAt = finishedAt)
        rebuildSummariesFlow()
        return Result.Success(Unit)
    }

    private fun rebuildSummariesFlow() {
        _summariesFlow.value = _games.values
            .sortedByDescending { it.id }
            .map { game ->
                GameSummary(
                    id = game.id,
                    status = game.status,
                    playerCount = game.config.players.size,
                    turnCount = 0,
                    startedAt = game.startedAt,
                    finishedAt = game.finishedAt
                )
            }
    }
}