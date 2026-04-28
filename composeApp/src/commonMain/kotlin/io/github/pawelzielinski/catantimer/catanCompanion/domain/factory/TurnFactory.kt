package io.github.pawelzielinski.catantimer.catanCompanion.domain.factory

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn

object TurnFactory {

    fun createFirst(game: Game): Turn {
        val firstPlayer = game.players.first()
        val secondaryPlayer = if (game.specialTurnRuleEnabled) {
            game.players[3 % game.players.size]
        } else null

        return Turn(
            number = 0,
            gameId = game.id,
            playerId = firstPlayer.playerId,
            playerName = firstPlayer.playerName,
            secondaryPlayerId = secondaryPlayer?.playerId,
            secondaryPlayerName = secondaryPlayer?.playerName
        )
    }

    fun createNext(currentTurn: Turn, game: Game): Turn {
        val nextNumber = currentTurn.number + 1
        val players = game.players
        val playerIndex = nextNumber % players.size
        val nextPlayer = players[playerIndex]
        val secondaryPlayer = if (game.specialTurnRuleEnabled) {
            players[(playerIndex + 3) % players.size]
        } else null

        return Turn(
            number = nextNumber,
            gameId = game.id,
            playerId = nextPlayer.playerId,
            playerName = nextPlayer.playerName,
            secondaryPlayerId = secondaryPlayer?.playerId,
            secondaryPlayerName = secondaryPlayer?.playerName
        )
    }
}
