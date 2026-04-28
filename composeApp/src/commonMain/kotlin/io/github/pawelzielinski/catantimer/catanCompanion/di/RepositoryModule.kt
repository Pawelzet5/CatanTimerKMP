package io.github.pawelzielinski.catantimer.catanCompanion.di

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.github.pawelzielinski.catantimer.catanCompanion.data.local.CatanCompanionDatabase
import io.github.pawelzielinski.catantimer.catanCompanion.data.repository.RoomGameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.repository.RoomPlayerRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.repository.RoomTurnRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.PlayerRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.TurnRepository
import io.github.pawelzielinski.catantimer.core.data.TransactionRunner
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { RoomPlayerRepository(get()) }
    single<TransactionRunner> {
        val db = get<CatanCompanionDatabase>()
        object : TransactionRunner {
            override suspend fun <T> run(block: suspend () -> T): T =
                db.useWriterConnection { it.immediateTransaction { block() } }
        }
    }
    single<GameRepository> { RoomGameRepository(get(), get(), get(), get()) }
    single<TurnRepository> { RoomTurnRepository(get(), get()) }
}
