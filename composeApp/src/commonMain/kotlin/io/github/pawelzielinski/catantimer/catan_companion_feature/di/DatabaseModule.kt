package io.github.pawelzielinski.catantimer.catan_companion_feature.di

import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.CatanCompanionDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { get<CatanCompanionDatabase>().playerDao() }
    single { get<CatanCompanionDatabase>().gameDao() }
    single { get<CatanCompanionDatabase>().gamePlayerDao() }
    single { get<CatanCompanionDatabase>().turnDao() }
}
// Note: the CatanCompanionDatabase instance is registered in the platform-specific
// coreModule (Modules.kt) via DatabaseFactory, which is provided by each platform module.
