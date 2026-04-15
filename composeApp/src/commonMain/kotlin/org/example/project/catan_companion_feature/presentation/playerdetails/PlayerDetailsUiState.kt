package org.example.project.catan_companion_feature.presentation.playerdetails

import org.example.project.catan_companion_feature.domain.dataclass.Player

data class PlayerDetailsUiState(
    val player: Player? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
