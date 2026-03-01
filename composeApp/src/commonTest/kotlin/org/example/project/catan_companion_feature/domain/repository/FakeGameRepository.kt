package org.example.project.catan_companion_feature.domain.repository

import org.example.project.core.util.DataError
import org.example.project.core.util.EmptyResult
import org.example.project.core.util.Result
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.catan_companion_feature.domain.enums.GameStatus

class FakeGameRepository : GameRepository {

    private val _games = mutableMapOf<Long, Game>()
    val games: List<Game> get() = _games.values.toList()

    var shouldFailOnGetGame = false
    var shouldFailOnGetActiveGame = false
    var shouldFailOnGetGames = false
    var shouldFailOnAddGame = false
    var shouldFailOnFinishGame = false

    private var nextId = 1L

    fun addGame(game: Game) {
        _games[game.id] = game
    }

    override suspend fun addGame(config: GameConfig): Result<Long, DataError.Local> {
        if (shouldFailOnAddGame) return Result.Failure(DataError.Local.UNKNOWN)
        val id = nextId++
        _games[id] = Game(id = id, config = config, status = GameStatus.ACTIVE)
        return Result.Success(id)
    }

    override suspend fun getGame(gameId: Long): Result<Game, DataError.Local> {
        if (shouldFailOnGetGame) return Result.Failure(DataError.Local.UNKNOWN)
        return _games[gameId]
            ?.let { Result.Success(it) }
            ?: Result.Failure(DataError.Local.NOT_FOUND)
    }

    override suspend fun getActiveGame(): Result<Game, DataError.Local> {
        if (shouldFailOnGetActiveGame) return Result.Failure(DataError.Local.UNKNOWN)
        return _games.values.firstOrNull { it.status == GameStatus.ACTIVE }
            ?.let { Result.Success(it) }
            ?: Result.Failure(DataError.Local.NOT_FOUND)
    }

    override suspend fun getGames(): Result<List<Game>, DataError.Local> {
        if (shouldFailOnGetGames) return Result.Failure(DataError.Local.UNKNOWN)
        return Result.Success(_games.values.toList())
    }

    override suspend fun saveGameAsFinished(gameId: Long): EmptyResult<DataError.Local> {
        if (shouldFailOnFinishGame) return Result.Failure(DataError.Local.UNKNOWN)
        val game = _games[gameId] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _games[gameId] = game.copy(status = GameStatus.FINISHED)
        return Result.Success(Unit)
    }
}