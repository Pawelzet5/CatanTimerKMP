package io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass

data class GamePlayer(
    val gameId: Long,
    val playerId: Long,
    val playerName: String,
    val orderIndex: Int
)
