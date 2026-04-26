package io.github.pawelzielinski.catantimer.catan_companion_feature.di

import io.github.pawelzielinski.catantimer.catan_companion_feature.data.repository.RoomGameRepository
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.repository.RoomPlayerRepository
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.repository.RoomTurnRepository
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.repository.PlayerRepository
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.repository.TurnRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { RoomPlayerRepository(get()) }
    single<GameRepository> { RoomGameRepository(get(), get(), get(), get()) }
    single<TurnRepository> { RoomTurnRepository(get(), get()) }
}
