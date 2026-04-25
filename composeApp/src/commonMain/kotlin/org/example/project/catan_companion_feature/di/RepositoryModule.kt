package org.example.project.catan_companion_feature.di

import org.example.project.catan_companion_feature.data.repository.RoomGameRepository
import org.example.project.catan_companion_feature.data.repository.RoomPlayerRepository
import org.example.project.catan_companion_feature.data.repository.RoomTurnRepository
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { RoomPlayerRepository(get()) }
    single<GameRepository> { RoomGameRepository(get(), get(), get(), get()) }
    single<TurnRepository> { RoomTurnRepository(get(), get()) }
}
