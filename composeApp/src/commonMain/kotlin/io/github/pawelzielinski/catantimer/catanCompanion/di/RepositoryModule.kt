package io.github.pawelzielinski.catantimer.catanCompanion.di

import io.github.pawelzielinski.catantimer.catanCompanion.data.repository.RoomGameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.repository.RoomPlayerRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.repository.RoomTurnRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.PlayerRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.TurnRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { RoomPlayerRepository(get()) }
    single<GameRepository> { RoomGameRepository(get(), get(), get(), get()) }
    single<TurnRepository> { RoomTurnRepository(get(), get()) }
}
