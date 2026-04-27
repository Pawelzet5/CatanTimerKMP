package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerslist

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player

sealed interface PlayersListEvent {
    data object NavigateBack : PlayersListEvent
    data class NavigateToPlayerDetails(val playerId: Long) : PlayersListEvent
    data class PlayersSelected(val players: List<Player>) : PlayersListEvent
}
