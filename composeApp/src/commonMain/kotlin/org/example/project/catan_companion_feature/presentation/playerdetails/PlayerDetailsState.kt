package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerdetails

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player

data class PlayerDetailsState(
    val player: Player? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
