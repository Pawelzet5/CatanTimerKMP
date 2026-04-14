package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
import org.example.project.catan_companion_feature.domain.dataclass.Player

fun PlayerEntity.toDomain(gamesPlayed: Int = 0, gamesWon: Int = 0): Player = Player(
    id = id,
    name = name,
    isHidden = isHidden,
    gamesPlayed = gamesPlayed,
    gamesWon = gamesWon
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    isHidden = isHidden
)
