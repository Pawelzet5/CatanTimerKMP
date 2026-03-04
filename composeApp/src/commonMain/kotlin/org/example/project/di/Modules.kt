package org.example.project.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.example.project.catan_companion_feature.data.local.CatanTimerDatabase
import org.example.project.catan_companion_feature.data.local.DatabaseFactory
import org.example.project.catan_companion_feature.data.repository.GameRepositoryImpl
import org.example.project.catan_companion_feature.data.repository.PlayerRepositoryImpl
import org.example.project.catan_companion_feature.data.repository.TurnRepositoryImpl
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val coreModule = module {
    single {
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}


val productModule = module {
    single { get<CatanTimerDatabase>().turnDao() }
    single { get<CatanTimerDatabase>().playerDao() }
    single { get<CatanTimerDatabase>().gameDao() }

    singleOf(::GameRepositoryImpl).bind<GameRepository>()
    singleOf(::PlayerRepositoryImpl).bind<PlayerRepository>()
    singleOf(::TurnRepositoryImpl).bind<TurnRepository>()
}