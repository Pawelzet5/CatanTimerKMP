package org.example.project.catan_companion_feature.domain.dataclass

import org.example.project.catan_companion_feature.domain.enums.EventDiceType

data class BarbarianState(
    val position: Int,                  // 0–7 (0 = start, 7 = island)
    val raidsCompleted: Int,
    val hasFirstRaidOccurred: Boolean
)

fun List<Turn>.toBarbarianState(): BarbarianState {
    val barbarianRolls = count { it.eventDice == EventDiceType.BARBARIANS }
    val position = barbarianRolls % 8
    val raidsCompleted = barbarianRolls / 8
    return BarbarianState(
        position = position,
        raidsCompleted = raidsCompleted,
        hasFirstRaidOccurred = raidsCompleted > 0 || (position == 0 && barbarianRolls > 0)
    )
}
