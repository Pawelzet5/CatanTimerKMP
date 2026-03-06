package org.example.project.catan_companion_feature.data.fakes.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity

class FakePlayerDao : PlayerDao {

    private val _players = mutableMapOf<Long, PlayerEntity>()

    /**
     * gameId → ordered list of playerIds (order mirrors playerIndex from cross-ref).
     * Populated via [setPlayersForGame] in test setup.
     */
    private val _gamePlayerMap = mutableMapOf<Long, List<Long>>()

    private val _playersState = MutableStateFlow<List<PlayerEntity>>(emptyList())

    // region error flags

    /**
     *SQLiteConstraintException is not a subclass of SQLiteException in androidx.sqlite —
     *Room wraps it as a plain RuntimeException, so tryLocalWrite maps it to UNKNOWN.
     *Simulates Room throwing SQLiteConstraintException when player is linked to a game/turn.
     */
    var shouldThrowConstraintExceptionOnDelete = false

    // endregion

    // region state controls

    var lastInsertedId: Long = 0L
        private set

    private var nextId = 1L

    /**
     * Prepopulates the DAO with existing entities, bypassing id generation.
     * Use when you need players in _players map without going through the repository
     * (e.g. when setting up getGame tests that use setPlayersForGame).
     */
    fun addPlayers(vararg players: PlayerEntity) {
        players.forEach { _players[it.id] = it }
        nextId = (_players.keys.maxOrNull() ?: 0L) + 1L
        _playersState.value = _players.values.toList()
    }

    /**
     * Defines which players belong to a game, in playerIndex order.
     * Call this in test setup after inserting players via the repository.
     */
    fun setPlayersForGame(gameId: Long, playerIds: List<Long>) {
        _gamePlayerMap[gameId] = playerIds
    }

    // endregion

    // region PlayerDao impl

    override suspend fun insertPlayer(player: PlayerEntity): Long {
        // Zawsze generujemy nowe id — ignorujemy player.id z encji wejściowej.
        // Room również ignoruje wartość pola @PrimaryKey(autoGenerate=true) przy INSERT
        // i zawsze przydziela własne id. Fake odwzorowuje to zachowanie.
        val id = nextId++
        val stored = player.copy(id = id)
        _players[id] = stored
        lastInsertedId = id
        _playersState.value = _players.values.toList()
        return id
    }

    override suspend fun getPlayer(playerId: Long): PlayerEntity? =
        _players[playerId]

    override suspend fun getAllPlayers(): List<PlayerEntity> =
        _players.values.sortedBy { it.name }

    /**
     * StateFlow.map tworzy cold Flow który przy każdej subskrypcji natychmiast emituje
     * wyfiltrowaną wartość aktualnego stanu, a następnie reaguje na każdą kolejną zmianę.
     * To dokładnie odwzorowuje zachowanie Room @Query z Flow.
     */
    override fun searchPlayers(query: String): Flow<List<PlayerEntity>> =
        _playersState.map { players ->
            players
                .filter { it.name.contains(query, ignoreCase = true) }
                .sortedBy { it.name }
        }

    override suspend fun getPlayersForGame(gameId: Long): List<PlayerEntity> {
        val ids = _gamePlayerMap[gameId] ?: return emptyList()
        return ids.mapNotNull { _players[it] }
    }

    override suspend fun getGameCountForPlayer(playerId: Long): Int =
        _gamePlayerMap.values.count { playerIds -> playerId in playerIds }

    override suspend fun deletePlayer(playerId: Long) {
        if (shouldThrowConstraintExceptionOnDelete) {
            throw RuntimeException("Fake: FOREIGN KEY constraint failed")
        }
        _players.remove(playerId)
        _playersState.value = _players.values.toList()
    }

    // endregion
}