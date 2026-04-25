package io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.pawelzielinski.catantimer.core.util.currentTimeMillis

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val isHidden: Boolean = false,
    val createdAt: Long = currentTimeMillis()
)