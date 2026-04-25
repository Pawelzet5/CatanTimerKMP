package io.github.pawelzielinski.catantimer.catan_companion_feature.data.local

import androidx.room.RoomDatabaseConstructor

// Reason for expect/actual: Room KSP generates the actual implementation per platform.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object DatabaseConstructor : RoomDatabaseConstructor<CatanCompanionDatabase> {
    override fun initialize(): CatanCompanionDatabase
}
