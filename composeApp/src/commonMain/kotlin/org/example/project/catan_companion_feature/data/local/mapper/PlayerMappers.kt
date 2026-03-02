package org.example.project.catan_companion_feature.data.local.mapper

import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
import org.example.project.catan_companion_feature.domain.dataclass.Player


fun PlayerEntity.toDomain(): Player = Player(id = id, name = name)

fun Player.toEntity(): PlayerEntity = PlayerEntity(id = if (id == -1L) 0L else id, name = name)