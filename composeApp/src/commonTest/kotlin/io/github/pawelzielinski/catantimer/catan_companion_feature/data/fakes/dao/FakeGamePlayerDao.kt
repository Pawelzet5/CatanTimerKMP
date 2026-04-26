package io.github.pawelzielinski.catantimer.catan_companion_feature.data.fakes.dao

import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.dao.GamePlayerDao
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.GamePlayerEntity

class FakeGamePlayerDao : GamePlayerDao {

    private val _entries = mutableListOf<GamePlayerEntity>()

    fun getEntries(): List<GamePlayerEntity> = _entries.toList()

    override suspend fun getForGame(gameId: Long): List<GamePlayerEntity> =
        _entries.filter { it.gameId == gameId }.sortedBy(GamePlayerEntity::orderIndex)

    override suspend fun insertAll(players: List<GamePlayerEntity>) {
        _entries.addAll(players)
    }

    override suspend fun deleteForGame(gameId: Long) {
        _entries.removeAll { it.gameId == gameId }
    }
}
