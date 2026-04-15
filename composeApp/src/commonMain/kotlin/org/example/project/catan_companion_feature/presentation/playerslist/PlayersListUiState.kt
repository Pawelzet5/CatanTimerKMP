package org.example.project.catan_companion_feature.presentation.playerslist

import org.example.project.catan_companion_feature.domain.dataclass.Player

data class PlayersListUiState(
    val players: List<Player> = emptyList(),
    val isSelectionMode: Boolean = false,
    val isLoading: Boolean = false
)
