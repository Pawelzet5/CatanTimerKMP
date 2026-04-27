package io.github.pawelzielinski.catantimer.catanCompanion.di

import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.CreateGameUseCase
import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.GetGameStatisticsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CreateGameUseCase(get()) }
    factory { GetGameStatisticsUseCase(get(), get()) }
}
