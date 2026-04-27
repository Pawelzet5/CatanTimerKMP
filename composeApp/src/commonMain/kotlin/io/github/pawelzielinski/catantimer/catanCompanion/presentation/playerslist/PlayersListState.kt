package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerslist

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player

data class PlayersListState(
    val players: List<Player> = emptyList(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false
)
