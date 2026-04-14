package org.example.project.catan_companion_feature.domain.factory

import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.domain.dataclass.Turn

object TurnFactory {

    fun createFirst(players: List<GamePlayer>, specialTurnRuleEnabled: Boolean): Turn {
        val firstPlayer = players.first()
        val secondaryPlayerId = if (specialTurnRuleEnabled) {
            players[3 % players.size].playerId
        } else null

        return Turn(
            number = 0,
            playerId = firstPlayer.playerId,
            secondaryPlayerId = secondaryPlayerId
        )
    }

    fun createNext(currentTurn: Turn, players: List<GamePlayer>, specialTurnRuleEnabled: Boolean): Turn {
        val nextNumber = currentTurn.number + 1
        val playerIndex = nextNumber % players.size
        val nextPlayer = players[playerIndex]
        val secondaryPlayerId = if (specialTurnRuleEnabled) {
            players[(playerIndex + 3) % players.size].playerId
        } else null

        return Turn(
            number = nextNumber,
            playerId = nextPlayer.playerId,
            secondaryPlayerId = secondaryPlayerId
        )
    }
}
