package org.example.project.catan_companion_feature.data.local

import androidx.room.RoomDatabase

// Reason for expect/actual: each platform provides a different Room builder mechanism
// (Android uses Context, iOS uses NSDocumentDirectory, Desktop uses file system path).
expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<CatanCompanionDatabase>
}
