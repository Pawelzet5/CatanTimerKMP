package org.example.project.catan_companion_feature.presentation.playerdetails

sealed interface PlayerDetailsAction {
    data object BackClick : PlayerDetailsAction
    data class UpdateName(val name: String) : PlayerDetailsAction
    data object HidePlayerClick : PlayerDetailsAction
    data object DeletePlayerClick : PlayerDetailsAction
}
