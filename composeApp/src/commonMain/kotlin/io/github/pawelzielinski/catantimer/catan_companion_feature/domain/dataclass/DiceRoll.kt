package io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType

data class DiceRoll(
    val red: Int,
    val yellow: Int,
    val event: EventDiceType? = null
) {
    val sum: Int get() = red + yellow
}
