package org.example.project.catan_companion_feature.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE games ADD COLUMN winnerId INTEGER")
        database.execSQL(
            "ALTER TABLE players ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0"
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS game_players (
                gameId INTEGER NOT NULL,
                playerId INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                PRIMARY KEY(gameId, playerId),
                FOREIGN KEY(gameId) REFERENCES games(id) ON DELETE CASCADE,
                FOREIGN KEY(playerId) REFERENCES players(id) ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        database.execSQL(
            "UPDATE games SET status = 'IN_PROGRESS' WHERE status = 'ACTIVE'"
        )
        database.execSQL(
            "UPDATE games SET status = 'COMPLETED' WHERE status = 'FINISHED'"
        )
    }
}
