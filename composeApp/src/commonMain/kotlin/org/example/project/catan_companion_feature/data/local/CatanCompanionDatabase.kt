package org.example.project.catan_companion_feature.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.example.project.catan_companion_feature.data.local.converter.EventDiceTypeConverter
import org.example.project.catan_companion_feature.data.local.converter.GameExpansionConverter
import org.example.project.catan_companion_feature.data.local.converter.GameStatusConverter
import org.example.project.catan_companion_feature.data.local.dao.GameDao
import org.example.project.catan_companion_feature.data.local.dao.GamePlayerDao
import org.example.project.catan_companion_feature.data.local.dao.PlayerDao
import org.example.project.catan_companion_feature.data.local.dao.TurnDao
import org.example.project.catan_companion_feature.data.local.entity.GameEntity
import org.example.project.catan_companion_feature.data.local.entity.GamePlayerEntity
import org.example.project.catan_companion_feature.data.local.entity.PlayerEntity
import org.example.project.catan_companion_feature.data.local.entity.TurnEntity

@Database(
    entities = [
        PlayerEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        TurnEntity::class
    ],
    version = 2,
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
