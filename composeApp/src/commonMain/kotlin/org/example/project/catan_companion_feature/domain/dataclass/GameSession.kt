package org.example.project.catan_companion_feature.domain.dataclass

data class GameSession(
    val game: Game,
    val latestTurn: Turn,
    val selectedTurn: Turn,
    val recentTurns: List<Turn>
) {

    val isActiveTurnSelected: Boolean
        get() = selectedTurn.id == latestTurn.id
}