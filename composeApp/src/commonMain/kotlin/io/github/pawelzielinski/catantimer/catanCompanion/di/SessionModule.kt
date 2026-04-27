package io.github.pawelzielinski.catantimer.catanCompanion.di

import io.github.pawelzielinski.catantimer.catanCompanion.domain.session.DefaultGameSessionCoordinator
import io.github.pawelzielinski.catantimer.catanCompanion.domain.session.GameSessionCoordinator
import org.koin.dsl.module

val sessionModule = module {
    // Singleton — one active session for the entire application lifecycle
    single<GameSessionCoordinator> { DefaultGameSessionCoordinator(get(), get()) }
}
