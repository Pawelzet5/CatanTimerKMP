package io.github.pawelzielinski.catantimer.catanCompanion.di

import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.CreateGameUseCase
import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.GetGameStatisticsUseCase
import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.UpdateGameSettingsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CreateGameUseCase(get()) }
    factory { GetGameStatisticsUseCase(get(), get()) }
    factory { UpdateGameSettingsUseCase(get()) }
}
