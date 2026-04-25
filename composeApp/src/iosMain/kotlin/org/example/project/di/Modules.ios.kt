package io.github.pawelzielinski.catantimer.di

import io.github.pawelzielinski.catantimer.catan_companion_feature.data.local.DatabaseFactory
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.service.HapticService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single { DatabaseFactory() }
        single { HapticService }
    }