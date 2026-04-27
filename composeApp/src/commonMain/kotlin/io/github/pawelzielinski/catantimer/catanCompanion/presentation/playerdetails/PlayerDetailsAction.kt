package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerdetails

sealed interface PlayerDetailsAction {
    data object BackClick : PlayerDetailsAction
    data class UpdateName(val name: String) : PlayerDetailsAction
    data object HidePlayerClick : PlayerDetailsAction
    data object DeletePlayerClick : PlayerDetailsAction
}
