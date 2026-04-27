package io.github.pawelzielinski.catantimer.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.DatabaseFactory
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
