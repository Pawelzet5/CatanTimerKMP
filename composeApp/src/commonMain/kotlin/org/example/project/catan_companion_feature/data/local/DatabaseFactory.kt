package org.example.project.catan_companion_feature.data.local

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<CatanTimerDatabase>
}