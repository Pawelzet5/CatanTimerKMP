package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game

data class GamesListState(
    val inProgressGames: List<Game> = emptyList(),
    val completedGames: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val gameToDelete: Game? = null
)
