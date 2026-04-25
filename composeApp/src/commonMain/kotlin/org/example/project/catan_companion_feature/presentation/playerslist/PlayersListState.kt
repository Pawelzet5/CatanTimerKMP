package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerslist

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player

data class PlayersListState(
    val players: List<Player> = emptyList(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false
)
