package org.example.project.di

import org.example.project.catan_companion_feature.presentation.dashboard.DashboardViewModel
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigViewModel
import org.example.project.catan_companion_feature.presentation.gameconfig.PlayersSelectionViewModel
import org.example.project.catan_companion_feature.presentation.gameplay.GameplayViewModel
import org.example.project.catan_companion_feature.presentation.gameslist.GamesListViewModel
import org.example.project.catan_companion_feature.presentation.gamesummary.GameSummaryViewModel
import org.example.project.catan_companion_feature.presentation.playerdetails.PlayerDetailsViewModel
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::DashboardViewModel)
    viewModelOf(::GameConfigViewModel)
    viewModelOf(::PlayersSelectionViewModel)
    // GameplayViewModel and others with navigation params (Long) use factory with parametersOf at the call site
    factory { params -> GameplayViewModel(params.get(), get(), get(), get()) }
    viewModelOf(::PlayersListViewModel)
    factory { params -> PlayerDetailsViewModel(params.get(), get()) }
    viewModelOf(::GamesListViewModel)
    factory { params -> GameSummaryViewModel(params.get(), get(), get()) }
}
