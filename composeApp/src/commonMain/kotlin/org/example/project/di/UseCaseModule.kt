package org.example.project.di

import org.example.project.catan_companion_feature.domain.usecase.CreateGameUseCase
import org.example.project.catan_companion_feature.domain.usecase.GetGameStatisticsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { CreateGameUseCase(get()) }
    factory { GetGameStatisticsUseCase(get(), get()) }
}
