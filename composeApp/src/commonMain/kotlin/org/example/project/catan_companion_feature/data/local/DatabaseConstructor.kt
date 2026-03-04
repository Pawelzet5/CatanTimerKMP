package org.example.project.catan_companion_feature.data.local

import androidx.room.RoomDatabaseConstructor

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object DatabaseConstructor: RoomDatabaseConstructor<CatanTimerDatabase> {
    override fun initialize(): CatanTimerDatabase
}