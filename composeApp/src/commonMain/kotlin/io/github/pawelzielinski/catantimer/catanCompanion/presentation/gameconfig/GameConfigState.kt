package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig

import io.github.pawelzielinski.catantimer.catanCompanion.AppConstants
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.GameExpansion
import io.github.pawelzielinski.catantimer.core.util.UiText

data class GameConfigState(
    val turnDurationMillis: Long = AppConstants.DEFAULT_TURN_DURATION_MS,
    val numberOfPlayers: Int = 4,
    val selectedPlayers: List<Player> = emptyList(),
    val availablePlayers: List<Player> = emptyList(),
    val expansions: Set<GameExpansion> = emptySet(),
    val specialTurnRuleEnabled: Boolean = false,
    val isValid: Boolean = false,
    val validationError: UiText? = null,
    val isLoading: Boolean = false
)
