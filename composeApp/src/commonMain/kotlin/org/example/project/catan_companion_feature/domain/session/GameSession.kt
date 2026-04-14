package org.example.project.catan_companion_feature.domain.session

import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.Turn

data class GameSession(
    val game: Game,
    val latestTurn: Turn,
    val selectedTurn: Turn,
    val recentTurns: List<Turn>
) {
    val isActiveTurnSelected: Boolean
        get() = selectedTurn.id == latestTurn.id
}
