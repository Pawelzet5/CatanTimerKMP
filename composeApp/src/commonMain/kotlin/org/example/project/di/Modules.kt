package org.example.project.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.example.project.catan_companion_feature.data.local.CatanTimerDatabase
import org.example.project.catan_companion_feature.data.local.DatabaseFactory
import org.koin.core.module.Module
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

    //singleOf(::GameRepositoryImpl).bind<GameRepository>()
}