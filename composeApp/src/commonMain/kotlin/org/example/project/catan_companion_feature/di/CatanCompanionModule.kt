package org.example.project.catan_companion_feature.di

import org.example.project.catan_companion_feature.presentation.dashboard.DashboardViewModel
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigViewModel
import org.example.project.catan_companion_feature.presentation.gameconfig.PlayersSelectionViewModel
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayViewModel
import org.example.project.catan_companion_feature.presentation.gameplay.TimerManager
import org.example.project.catan_companion_feature.presentation.gameslist.GamesListViewModel
import org.example.project.catan_companion_feature.presentation.gamesummary.GameSummaryViewModel
import org.example.project.catan_companion_feature.presentation.playerdetails.PlayerDetailsViewModel
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val catanCompanionModule = module {
    includes(databaseModule, repositoryModule, sessionModule, useCaseModule)

    factory { TimerManager() }

    viewModelOf(::DashboardViewModel)
    viewModelOf(::GameConfigViewModel)
    viewModelOf(::PlayersSelectionViewModel)
    viewModel { params ->
        GameplayViewModel(
            gameId = params.get(),
            sessionCoordinator = get(),
            turnRepository = get(),
            gameRepository = get(),
            timerManager = get()
        )
    }
    viewModelOf(::PlayersListViewModel)
    viewModel { params -> PlayerDetailsViewModel(params.get(), get()) }
    viewModelOf(::GamesListViewModel)
    viewModel { params -> GameSummaryViewModel(params.get(), get(), get()) }
}
