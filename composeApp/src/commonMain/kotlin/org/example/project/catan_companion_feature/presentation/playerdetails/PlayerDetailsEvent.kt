package org.example.project.catan_companion_feature.presentation.playerdetails

sealed interface PlayerDetailsEvent {
    data object NavigateBack : PlayerDetailsEvent
}
