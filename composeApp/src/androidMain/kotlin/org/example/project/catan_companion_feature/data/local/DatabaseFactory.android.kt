package org.example.project.catan_companion_feature.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<CatanCompanionDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(DatabaseConstants.DB_NAME)

        return Room.databaseBuilder<CatanCompanionDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}
