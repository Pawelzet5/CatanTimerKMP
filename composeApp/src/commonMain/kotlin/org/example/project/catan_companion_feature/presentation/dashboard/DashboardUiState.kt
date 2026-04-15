package org.example.project.catan_companion_feature.presentation.dashboard

import org.example.project.catan_companion_feature.domain.dataclass.Game

data class DashboardUiState(
    val resumableGame: Game? = null,
    val isLoading: Boolean = false
)
