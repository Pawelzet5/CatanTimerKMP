package org.example.project.catan_companion_feature.domain.dataclass

import org.example.project.catan_companion_feature.domain.enums.GameStatus

data class Game(
    val id: Long,
    val config: GameConfig,
    val status: GameStatus
)