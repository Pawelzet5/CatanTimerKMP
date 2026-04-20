package org.example.project.catan_companion_feature.presentation.playerslist

import org.example.project.catan_companion_feature.domain.dataclass.Player

sealed interface PlayersListEvent {
    data object NavigateBack : PlayersListEvent
    data class NavigateToPlayerDetails(val playerId: Long) : PlayersListEvent
    data class PlayersSelected(val players: List<Player>) : PlayersListEvent
}
