package org.example.project.catan_companion_feature.data.fakes.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.core.domain.DataError
import org.example.project.core.domain.EmptyResult
import org.example.project.core.domain.Result

class FakeGameRepository : GameRepository {

    private val _games = mutableMapOf<Long, Game>()
    private val _gamesState = MutableStateFlow<List<Game>>(emptyList())

    val games: List<Game> get() = _gamesState.value

    var shouldFailOnGetGame = false
    var shouldFailOnFinishGame = false

    private var nextId = 1L

    /**
     * Prepopulates the repository with an existing [Game] for test setup.
     * Bypasses id-generation — id is set by the caller.
     */
    fun seedGame(game: Game) {
        _games[game.id] = game
        _gamesState.value = _games.values.toList()
    }

    fun setGame(game: Game) = seedGame(game)

    fun setInProgressGame(game: Game) = seedGame(game)

    override fun getAllGames(): Flow<List<Game>> =
        _gamesState.map { it.sortedByDescending(Game::startedAt) }

    override fun getInProgressGames(): Flow<List<Game>> =
        _gamesState.map { games ->
            games.filter { it.status == GameStatus.IN_PROGRESS }
                .sortedByDescending(Game::startedAt)
        }

    override fun getCompletedGames(): Flow<List<Game>> =
        _gamesState.map { games ->
            games.filter { it.status == GameStatus.COMPLETED }
                .sortedByDescending(Game::startedAt)
        }

    override fun getGameById(id: Long): Flow<Game?> = flow {
        if (shouldFailOnGetGame) throw Exception("Fake: get game failure")
        emit(_games[id])
    }

    override fun getMostRecentInProgressGame(): Flow<Game?> =
        _gamesState.map { games ->
            games.filter { it.status == GameStatus.IN_PROGRESS }
                .maxByOrNull(Game::startedAt)
        }

    override suspend fun createGame(
        turnDurationMillis: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean,
        playerIds: List<Long>
    ): Result<Long, DataError.Local> {
        val id = nextId++
        val game = Game(
            id = id,
            turnDurationMillis = turnDurationMillis,
            expansions = expansions,
            specialTurnRuleEnabled = specialTurnRuleEnabled,
            status = GameStatus.IN_PROGRESS,
            startedAt = 0L,
            players = playerIds.mapIndexed { index, playerId ->
                GamePlayer(gameId = id, playerId = playerId, playerName = "", orderIndex = index)
            }
        )
        _games[id] = game
        _gamesState.value = _games.values.toList()
        return Result.Success(id)
    }

    override suspend fun updateGameSettings(
        gameId: Long,
        expansions: Set<GameExpansion>,
        specialTurnRuleEnabled: Boolean
    ): EmptyResult<DataError.Local> {
        val game = _games[gameId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _games[gameId] = game.copy(
            expansions = expansions,
            specialTurnRuleEnabled = specialTurnRuleEnabled
        )
        _gamesState.value = _games.values.toList()
        return Result.Success(Unit)
    }

    override suspend fun endGame(gameId: Long, winnerId: Long?): EmptyResult<DataError.Local> {
        if (shouldFailOnFinishGame) return Result.Failure(DataError.Local.UNKNOWN)
        val game = _games[gameId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _games[gameId] = game.copy(status = GameStatus.COMPLETED, winnerId = winnerId)
        _gamesState.value = _games.values.toList()
        return Result.Success(Unit)
    }

    override suspend fun deleteGame(id: Long): EmptyResult<DataError.Local> {
        _games.remove(id) ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _gamesState.value = _games.values.toList()
        return Result.Success(Unit)
    }
}
