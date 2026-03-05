package org.example.project.catan_companion_feature.data.fakes.dao

import androidx.sqlite.SQLiteException
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

    // Backing flow for searchPlayers — updated on every insert/delete
    private val _playersFlow = MutableStateFlow<List<PlayerEntity>>(emptyList())

    // region error flags

    var shouldThrowSQLiteExceptionOnInsert = false
    var shouldThrowUnexpectedExceptionOnInsert = false

    var shouldThrowSQLiteExceptionOnRead = false
    var shouldThrowUnexpectedExceptionOnRead = false

    var shouldThrowSQLiteExceptionOnGetForGame = false
    var shouldThrowUnexpectedExceptionOnGetForGame = false

    var shouldThrowSQLiteExceptionOnDelete = false

    /**
     * Simulates a foreign key constraint violation on delete (e.g. player linked to a game or turn).
     *
     * Note: in androidx.sqlite, SQLiteConstraintException is NOT a subclass of SQLiteException —
     * Room wraps it in a plain RuntimeException on affected versions. We therefore throw
     * RuntimeException here, which [tryLocalWrite] maps to [DataError.Local.UNKNOWN].
     */
    var shouldThrowForeignKeyConstraintViolationOnDelete = false

    // endregion

    // region state controls

    var lastInsertedId: Long = 0L
        private set

    private var nextId = 1L

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
        if (shouldThrowSQLiteExceptionOnInsert) throw SQLiteException("Fake: disk full")
        if (shouldThrowUnexpectedExceptionOnInsert) throw RuntimeException("Fake: unexpected error")

        val id = if (player.id != 0L) player.id else nextId++
        val stored = player.copy(id = id)
        _players[id] = stored
        lastInsertedId = id
        _playersFlow.value = _players.values.toList()
        return id
    }

    override suspend fun getPlayer(playerId: Long): PlayerEntity? {
        if (shouldThrowSQLiteExceptionOnRead) throw SQLiteException("Fake: read error")
        if (shouldThrowUnexpectedExceptionOnRead) throw RuntimeException("Fake: unexpected error")

        return _players[playerId]
    }

    override suspend fun getAllPlayers(): List<PlayerEntity> {
        if (shouldThrowSQLiteExceptionOnRead) throw SQLiteException("Fake: read error")
        if (shouldThrowUnexpectedExceptionOnRead) throw RuntimeException("Fake: unexpected error")

        return _players.values.sortedBy { it.name }
    }

    override fun searchPlayers(query: String): Flow<List<PlayerEntity>> =
        _playersFlow.map { players ->
            players
                .filter { it.name.contains(query, ignoreCase = true) }
                .sortedBy { it.name }
        }

    override suspend fun getPlayersForGame(gameId: Long): List<PlayerEntity> {
        if (shouldThrowSQLiteExceptionOnGetForGame) throw SQLiteException("Fake: read error")
        if (shouldThrowUnexpectedExceptionOnGetForGame) throw RuntimeException("Fake: unexpected error")

        val ids = _gamePlayerMap[gameId] ?: return emptyList()
        return ids.mapNotNull { _players[it] }
    }

    override suspend fun getGameCountForPlayer(playerId: Long): Int =
        _gamePlayerMap.values.count { playerIds -> playerId in playerIds }

    override suspend fun deletePlayer(playerId: Long) {
        if (shouldThrowSQLiteExceptionOnDelete) throw SQLiteException("Fake: disk full")
        if (shouldThrowForeignKeyConstraintViolationOnDelete) throw RuntimeException("Fake: FOREIGN KEY constraint failed")

        _players.remove(playerId)
        _playersFlow.value = _players.values.toList()
    }

    // endregion
}