package org.example.project.catan_companion_feature.presentation.state

import org.example.project.catan_companion_feature.domain.dataclass.Game

data class GamesListUiState(
    val inProgressGames: List<Game> = emptyList(),
    val completedGames: List<Game> = emptyList(),
    val isLoading: Boolean = false
)
