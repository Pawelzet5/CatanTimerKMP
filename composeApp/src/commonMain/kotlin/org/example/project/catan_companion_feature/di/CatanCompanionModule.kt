package io.github.pawelzielinski.catantimer.catan_companion_feature.di

import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.dashboard.DashboardViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameconfig.GameConfigViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameconfig.PlayersSelectionViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay.GameplayViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist.GamesListViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gamesummary.GameSummaryViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerdetails.PlayerDetailsViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.playerslist.PlayersListViewModel
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
