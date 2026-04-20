package org.example.project.di

import org.example.project.catan_companion_feature.domain.session.GameSessionCoordinator
import org.example.project.catan_companion_feature.domain.session.DefaultGameSessionCoordinator
import org.koin.dsl.module

val sessionModule = module {
    // Singleton — one active session for the entire application lifecycle
    single<GameSessionCoordinator> { DefaultGameSessionCoordinator(get(), get()) }
}
