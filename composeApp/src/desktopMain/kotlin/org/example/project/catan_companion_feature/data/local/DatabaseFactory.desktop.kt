package org.example.project.catan_companion_feature.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.example.project.catan_companion_feature.AppConstants
import java.io.File

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<CatanCompanionDatabase> {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        val appDataDir = when {
            os.contains("win") -> File(System.getenv("APPDATA"), AppConstants.APP_NAME)
            os.contains("mac") -> File(userHome, "Library/Application Support/${AppConstants.APP_NAME}")
            else -> File(userHome, ".local/share/${AppConstants.APP_NAME}")
        }

        if (!appDataDir.exists())
            appDataDir.mkdirs()

        val dbFile = File(appDataDir, DatabaseConstants.DB_NAME)
        return Room.databaseBuilder<CatanCompanionDatabase>(dbFile.absolutePath)
    }
}
