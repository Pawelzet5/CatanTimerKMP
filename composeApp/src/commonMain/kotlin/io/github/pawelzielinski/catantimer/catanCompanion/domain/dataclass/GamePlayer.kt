package io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass

data class GamePlayer(
    val gameId: Long,
    val playerId: Long,
    val playerName: String,
    val orderIndex: Int
)
