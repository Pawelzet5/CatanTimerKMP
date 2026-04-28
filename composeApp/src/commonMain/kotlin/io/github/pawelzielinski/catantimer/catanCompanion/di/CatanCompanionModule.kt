package io.github.pawelzielinski.catantimer.catanCompanion.di

import io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard.DashboardViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig.GameConfigViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameconfig.PlayersSelectionViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay.GameplayViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameslist.GamesListViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary.GameSummaryViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerdetails.PlayerDetailsViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerslist.PlayersListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val catanCompanionModule = module {
    includes(databaseModule, repositoryModule, sessionModule, useCaseModule)

    viewModelOf(::DashboardViewModel)
    viewModelOf(::GameConfigViewModel)
    viewModelOf(::PlayersSelectionViewModel)
    viewModel { params -> GameplayViewModel(params.get(), get(), get(), get(), get()) }
    viewModelOf(::PlayersListViewModel)
    viewModel { params -> PlayerDetailsViewModel(params.get(), get()) }
    viewModel { GamesListViewModel(get(), get()) }
    viewModel { params -> GameSummaryViewModel(params.get(), get(), get()) }
}
