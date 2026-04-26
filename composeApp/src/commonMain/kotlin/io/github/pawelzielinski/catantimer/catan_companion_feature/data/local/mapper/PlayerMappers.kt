package io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.mapper

import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity.PlayerEntity
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.core.util.currentTimeMillis

fun PlayerEntity.toDomain(gamesPlayed: Int = 0, gamesWon: Int = 0): Player = Player(
    id = id,
    name = name,
    isHidden = isHidden,
    gamesPlayed = gamesPlayed,
    gamesWon = gamesWon,
    createdAt = createdAt
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    isHidden = isHidden,
    createdAt = createdAt ?: currentTimeMillis()
)
