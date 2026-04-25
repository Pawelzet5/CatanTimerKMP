package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameconfig

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameExpansion

sealed interface GameConfigAction {
    data object BackClick : GameConfigAction
    data class PlayerToggled(val player: Player) : GameConfigAction
    data class ExpansionToggled(val expansion: GameExpansion) : GameConfigAction
    data object SpecialTurnRuleToggled : GameConfigAction
    data class TurnDurationChanged(val millis: Long) : GameConfigAction
    data class PlayersSelected(val players: List<Player>) : GameConfigAction
    data class PlayerCountSelected(val count: Int) : GameConfigAction
    data object StartGameClick : GameConfigAction
    data object AddPlayerClick : GameConfigAction
    data class PlayersReordered(val players: List<Player>) : GameConfigAction
}
