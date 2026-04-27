package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameslist

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game

data class GamesListState(
    val inProgressGames: List<Game> = emptyList(),
    val completedGames: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val gameToDelete: Game? = null
)
