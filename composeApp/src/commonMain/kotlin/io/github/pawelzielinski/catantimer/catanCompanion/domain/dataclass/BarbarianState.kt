package io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass

import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType

data class BarbarianState(
    val position: Int,                  // 0–MAX_POSITION (0 = start, MAX_POSITION = island)
    val raidsCompleted: Int,
    val hasFirstRaidOccurred: Boolean
) {
    companion object {
        const val MAX_POSITION = 7
    }
}

fun List<Turn>.toBarbarianState(): BarbarianState {
    val barbarianRolls = count { it.eventDice == EventDiceType.BARBARIANS }
    val position = barbarianRolls % (BarbarianState.MAX_POSITION + 1)
    val raidsCompleted = barbarianRolls / (BarbarianState.MAX_POSITION + 1)
    return BarbarianState(
        position = position,
        raidsCompleted = raidsCompleted,
        hasFirstRaidOccurred = raidsCompleted > 0 || (position == 0 && barbarianRolls > 0)
    )
}
