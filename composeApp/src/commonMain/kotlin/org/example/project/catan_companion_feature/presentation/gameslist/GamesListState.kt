package org.example.project.catan_companion_feature.presentation.gameslist

import org.example.project.catan_companion_feature.domain.dataclass.Game

data class GamesListState(
    val inProgressGames: List<Game> = emptyList(),
    val completedGames: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val gameToDelete: Game? = null
)
