package org.example.project.catan_companion_feature.domain.dataclass

data class GameSession(
    val game: Game,
    val selectedTurn: Turn,
    val recentTurns: List<Turn>
) {

    val isActiveTurnSelected: Boolean
        get() = recentTurns
            .maxOfOrNull { it.number }
            ?.let { selectedTurn.number > it }
            ?: true
}