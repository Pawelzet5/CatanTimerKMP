package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.Game

sealed interface GamesListAction {
    data object BackClick : GamesListAction
    data class GameClick(val gameId: Long) : GamesListAction
    data class RequestDeleteGame(val game: Game) : GamesListAction
    data object ConfirmDeleteGame : GamesListAction
    data object DismissDeleteGame : GamesListAction
}
