package io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.dao.GameDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GameEntity
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameStatus

class FakeGameDao : GameDao {

    private val _games = mutableMapOf<Long, GameEntity>()
    private val _gamesState = MutableStateFlow<List<GameEntity>>(emptyList())

    // region state controls

    var lastInsertedGameId: Long = 0L
        private set

    private var nextId = 1L

    // endregion

    // region GameDao impl

    override fun getAll(): Flow<List<GameEntity>> =
        _gamesState.map { it.sortedByDescending(GameEntity::startedAt) }

    override fun getInProgress(): Flow<List<GameEntity>> =
        _gamesState.map { games ->
            games.filter { it.status == GameStatus.IN_PROGRESS }
                .sortedByDescending(GameEntity::startedAt)
        }

    override fun getCompleted(): Flow<List<GameEntity>> =
        _gamesState.map { games ->
            games.filter { it.status == GameStatus.COMPLETED }
                .sortedByDescending(GameEntity::finishedAt)
        }

    override fun getById(id: Long): Flow<GameEntity?> =
        _gamesState.map { games -> games.find { it.id == id } }

    override fun getMostRecentInProgress(): Flow<GameEntity?> =
        _gamesState.map { games ->
            games.filter { it.status == GameStatus.IN_PROGRESS }
                .maxByOrNull(GameEntity::startedAt)
        }

    override suspend fun insert(game: GameEntity): Long {
        val id = if (game.id != 0L) game.id else nextId++
        val stored = game.copy(id = id)
        _games[id] = stored
        lastInsertedGameId = id
        _gamesState.value = _games.values.toList()
        return id
    }

    override suspend fun update(game: GameEntity) {
        if (_games.containsKey(game.id)) {
            _games[game.id] = game
            _gamesState.value = _games.values.toList()
        }
    }

    override suspend fun delete(game: GameEntity) {
        _games.remove(game.id)
        _gamesState.value = _games.values.toList()
    }

    // endregion
}
