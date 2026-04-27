package io.github.pawelzielinski.catantimer.catanCompanion.domain.session

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn

data class GameSession(
    val game: Game,
    val latestTurn: Turn,
    val selectedTurn: Turn,
    val recentTurns: List<Turn>
) {
    val isActiveTurnSelected: Boolean
        get() = selectedTurn.id == latestTurn.id
}
