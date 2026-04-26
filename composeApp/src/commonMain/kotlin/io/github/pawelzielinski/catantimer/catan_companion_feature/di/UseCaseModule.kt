package io.github.pawelzielinski.catantimer.catan_companion_feature.di

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.usecase.CreateGameUseCase
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.usecase.GetGameStatisticsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CreateGameUseCase(get()) }
    factory { GetGameStatisticsUseCase(get(), get()) }
}
