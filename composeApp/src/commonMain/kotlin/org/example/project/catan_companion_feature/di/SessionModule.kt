package org.example.project.catan_companion_feature.di

import org.example.project.catan_companion_feature.domain.session.DefaultGameSessionCoordinator
import org.example.project.catan_companion_feature.domain.session.GameSessionCoordinator
import org.koin.dsl.module

val sessionModule = module {
    // Singleton — one active session for the entire application lifecycle
    single<GameSessionCoordinator> { DefaultGameSessionCoordinator(get(), get()) }
}
