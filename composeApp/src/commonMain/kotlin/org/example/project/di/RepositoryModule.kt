package org.example.project.di

import org.example.project.catan_companion_feature.data.repository.GameRepositoryImpl
import org.example.project.catan_companion_feature.data.repository.PlayerRepositoryImpl
import org.example.project.catan_companion_feature.data.repository.TurnRepositoryImpl
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<GameRepository> { GameRepositoryImpl(get(), get(), get(), get()) }
    single<TurnRepository> { TurnRepositoryImpl(get(), get()) }
}
