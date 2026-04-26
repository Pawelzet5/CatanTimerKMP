package io.github.pawelzielinski.catantimer.catan_companion_feature.data.fakes.dao

import kotlinx.coroutines.flow.*
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.dao.PlayerDao
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.PlayerEntity

class FakePlayerDao : PlayerDao {

    private val _players = mutableMapOf<Long, PlayerEntity>()
    private val _playersState = MutableStateFlow<List<PlayerEntity>>(emptyList())

    // region state controls

    var lastInsertedId: Long = 0L
        private set

    private var nextId = 1L

    fun addPlayers(vararg players: PlayerEntity) {
        players.forEach { _players[it.id] = it }
        nextId = (_players.keys.maxOrNull() ?: 0L) + 1L
        _playersState.value = _players.values.toList()
    }

    // endregion

    // region PlayerDao impl

    override fun getAll(): Flow<List<PlayerEntity>> =
        _playersState.map { it.sortedBy(PlayerEntity::name) }

    override fun getVisible(): Flow<List<PlayerEntity>> =
        _playersState.map { players ->
            players.filter { !it.isHidden }.sortedBy(PlayerEntity::name)
        }

    override fun getById(id: Long): Flow<PlayerEntity?> =
        _playersState.map { players -> players.find { it.id == id } }

    override suspend fun getByIdOnce(id: Long): PlayerEntity? =
        _playersState.first().firstOrNull { it.id == id }


    override suspend fun insert(player: PlayerEntity): Long {
        val id = nextId++
        val stored = player.copy(id = id)
        _players[id] = stored
        lastInsertedId = id
        _playersState.value = _players.values.toList()
        return id
    }

    override suspend fun update(player: PlayerEntity) {
        if (_players.containsKey(player.id)) {
            _players[player.id] = player
            _playersState.value = _players.values.toList()
        }
    }

    override suspend fun hide(id: Long) {
        val player = _players[id] ?: return
        _players[id] = player.copy(isHidden = true)
        _playersState.value = _players.values.toList()
    }

    override suspend fun delete(player: PlayerEntity) {
        _players.remove(player.id)
        _playersState.value = _players.values.toList()
    }

    private val gameCountByPlayerId = mutableMapOf<Long, Int>()

    fun setGameCount(playerId: Long, count: Int) {
        gameCountByPlayerId[playerId] = count
    }

    override suspend fun getGameCount(playerId: Long): Int = gameCountByPlayerId[playerId] ?: 0

    // endregion
}
