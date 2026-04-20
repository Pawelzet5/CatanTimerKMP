package org.example.project.catan_companion_feature.presentation.gameslist

import org.example.project.catan_companion_feature.domain.dataclass.Game

sealed interface GamesListAction {
    data object BackClick : GamesListAction
    data class GameClick(val gameId: Long) : GamesListAction
    data class DeleteGameClick(val game: Game) : GamesListAction
}
