package io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass

data class DiceDistribution(
    val counts: Map<Int, Int>   // sum (2–12) -> roll count
)
