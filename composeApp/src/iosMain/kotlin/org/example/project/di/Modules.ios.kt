package org.example.project.di

import org.example.project.catan_companion_feature.data.local.DatabaseFactory
import org.example.project.catan_companion_feature.presentation.service.HapticService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single { DatabaseFactory() }
        single { HapticService() }
    }