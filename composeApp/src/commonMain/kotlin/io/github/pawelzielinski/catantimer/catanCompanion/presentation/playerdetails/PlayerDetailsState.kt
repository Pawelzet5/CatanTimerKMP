package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerdetails

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player

data class PlayerDetailsState(
    val player: Player? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
