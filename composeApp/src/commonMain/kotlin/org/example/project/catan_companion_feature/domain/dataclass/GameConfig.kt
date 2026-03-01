package org.example.project.catan_companion_feature.domain.dataclass

import org.example.project.catan_companion_feature.domain.enums.GameExpansion

data class GameConfig(
    val turnDurationMillis: Long,
    val expansions: Set<GameExpansion>,
    // Special turn is a in between turn that enabled 3rd next player to perform some limited actions
    // Therefore this flag is needed to properly manage appearance of the second timer for another player.
    val specialTurnRuleEnabled: Boolean,
    val players: List<Player>
) {
    init {
        require(!specialTurnRuleEnabled || players.size >= 5) {
            "Special turn rule requires at least 5 players"
        }
    }

    val hasEventDice: Boolean
        get() = GameExpansion.CITIES_AND_KNIGHTS in expansions
}
