package io.github.pawelzielinski.catantimer.di

import io.github.pawelzielinski.catantimer.catanCompanion.data.local.DatabaseFactory
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.service.HapticService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single { DatabaseFactory() }
        single { HapticService }
    }