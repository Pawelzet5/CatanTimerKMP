package io.github.pawelzielinski.catantimer.catanCompanion.data.local.mapper

import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.PlayerEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.PlayerWithGameCount
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Player
import io.github.pawelzielinski.catantimer.core.util.currentTimeMillis

fun PlayerWithGameCount.toDomain(): Player = player.toDomain(gamesPlayed = gamesPlayed)

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
