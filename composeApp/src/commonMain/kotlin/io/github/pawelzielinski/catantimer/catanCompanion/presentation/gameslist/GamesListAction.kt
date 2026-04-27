package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameslist

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game

sealed interface GamesListAction {
    data object BackClick : GamesListAction
    data class GameClick(val gameId: Long) : GamesListAction
    data class RequestDeleteGame(val game: Game) : GamesListAction
    data object ConfirmDeleteGame : GamesListAction
    data object DismissDeleteGame : GamesListAction
}
