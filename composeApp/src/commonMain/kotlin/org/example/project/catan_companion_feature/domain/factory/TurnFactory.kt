package org.example.project.catan_companion_feature.domain.factory

import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.domain.dataclass.Turn

object TurnFactory {

    fun createFirst(gameId: Long, players: List<GamePlayer>, specialTurnRuleEnabled: Boolean): Turn {
        val firstPlayer = players.first()
        val secondaryPlayer = if (specialTurnRuleEnabled) players[3 % players.size] else null
        return Turn(
            gameId = gameId,
            number = 0,
            playerId = firstPlayer.playerId,
            playerName = firstPlayer.playerName,
            secondaryPlayerId = secondaryPlayer?.playerId,
            secondaryPlayerName = secondaryPlayer?.playerName
        )
    }

    fun createNext(
        gameId: Long,
        currentTurn: Turn,
        players: List<GamePlayer>,
        specialTurnRuleEnabled: Boolean
    ): Turn {
        val nextNumber = currentTurn.number + 1
        val playerIndex = nextNumber % players.size
        val nextPlayer = players[playerIndex]
        val secondaryPlayer = if (specialTurnRuleEnabled) {
            players[(playerIndex + 3) % players.size]
        } else null
        return Turn(
            gameId = gameId,
            number = nextNumber,
            playerId = nextPlayer.playerId,
            playerName = nextPlayer.playerName,
            secondaryPlayerId = secondaryPlayer?.playerId,
            secondaryPlayerName = secondaryPlayer?.playerName
        )
    }
}
