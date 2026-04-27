package io.github.pawelzielinski.catantimer.catanCompanion.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.converter.EventDiceTypeConverter
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.converter.GameExpansionConverter
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.converter.GameStatusConverter
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.dao.GameDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.dao.GamePlayerDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.dao.PlayerDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.dao.TurnDao
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GameEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.GamePlayerEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.PlayerEntity
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.entity.TurnEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        TurnEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    GameExpansionConverter::class,
    GameStatusConverter::class,
    EventDiceTypeConverter::class
)
@ConstructedBy(DatabaseConstructor::class)
abstract class CatanCompanionDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun turnDao(): TurnDao
}
