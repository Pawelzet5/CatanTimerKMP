package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerslist

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player

sealed interface PlayersListAction {
    data object BackClick : PlayersListAction
    data class PlayerClick(val playerId: Long) : PlayersListAction
    data class DoneWithSelection(val players: List<Player>) : PlayersListAction
    data class CreatePlayer(val name: String) : PlayersListAction
    data class HidePlayer(val player: Player) : PlayersListAction
    data class DeletePlayer(val player: Player) : PlayersListAction
}
