package io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass

data class Player(
    val id: Long = 0L,
    val name: String,
    val isHidden: Boolean = false,
    val gamesPlayed: Int = 0,   // derived — populated by repository
    val gamesWon: Int = 0,      // derived — populated by repository
    val createdAt: Long? = null
)
