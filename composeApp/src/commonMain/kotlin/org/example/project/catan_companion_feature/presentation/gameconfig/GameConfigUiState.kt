package org.example.project.catan_companion_feature.presentation.gameconfig

import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameExpansion

data class GameConfigUiState(
    val turnDurationMillis: Long = 120_000L,
    val selectedPlayers: List<Player> = emptyList(),
    val availablePlayers: List<Player> = emptyList(),
    val expansions: Set<GameExpansion> = emptySet(),
    val specialTurnRuleEnabled: Boolean = false,
    val isValid: Boolean = false,
    val validationError: String? = null,
    val isLoading: Boolean = false
)
