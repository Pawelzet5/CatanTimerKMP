package org.example.project.catan_companion_feature.domain.dataclass

data class DiceDistribution(
    val counts: Map<Int, Int>   // sum (2–12) -> roll count
)
