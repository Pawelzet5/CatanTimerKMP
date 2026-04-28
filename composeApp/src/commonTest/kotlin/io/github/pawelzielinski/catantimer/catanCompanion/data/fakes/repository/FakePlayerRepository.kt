package io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.PlayerRepository
import io.github.pawelzielinski.catantimer.core.domain.DataError
import io.github.pawelzielinski.catantimer.core.domain.EmptyResult
import io.github.pawelzielinski.catantimer.core.domain.Result

class FakePlayerRepository : PlayerRepository {

    private val _players = mutableMapOf<Long, Player>()
    private val _playersState = MutableStateFlow<List<Player>>(emptyList())

    private val _createdPlayers = mutableListOf<String>()
    val createdPlayers: List<String> get() = _createdPlayers.toList()

    private var nextId = 1L

    fun setPlayers(players: List<Player>) {
        _players.clear()
        players.forEach { _players[it.id] = it }
        _playersState.value = _players.values.toList()
    }

    override fun getAllPlayers(): Flow<List<Player>> = _playersState

    override fun getVisiblePlayers(): Flow<List<Player>> =
        _playersState.map { players -> players.filter { !it.isHidden } }

    override fun getPlayerById(id: Long): Flow<Player?> =
        _playersState.map { players -> players.find { it.id == id } }

    override suspend fun createPlayer(name: String): Result<Long, DataError.Local> {
        val id = nextId++
        val player = Player(id = id, name = name)
        _players[id] = player
        _playersState.value = _players.values.toList()
        _createdPlayers.add(name)
        return Result.Success(id)
    }

    override suspend fun updatePlayer(player: Player): EmptyResult<DataError.Local> {
        if (!_players.containsKey(player.id)) return Result.Failure(DataError.Local.NOT_FOUND)
        _players[player.id] = player
        _playersState.value = _players.values.toList()
        return Result.Success(Unit)
    }

    override suspend fun hidePlayer(id: Long): EmptyResult<DataError.Local> {
        val player = _players[id] ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _players[id] = player.copy(isHidden = true)
        _playersState.value = _players.values.toList()
        return Result.Success(Unit)
    }

    override suspend fun deletePlayer(id: Long): EmptyResult<DataError.Local> {
        _players.remove(id) ?: return Result.Failure(DataError.Local.NOT_FOUND)
        _playersState.value = _players.values.toList()
        return Result.Success(Unit)
    }

    override suspend fun canDeletePlayer(id: Long): Boolean = _players.containsKey(id)
}
