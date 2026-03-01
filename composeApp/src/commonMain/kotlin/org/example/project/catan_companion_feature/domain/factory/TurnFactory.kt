package org.example.project.catan_companion_feature.domain.factory

import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.catan_companion_feature.domain.dataclass.Turn

object TurnFactory {

    fun createFirst(config: GameConfig): Turn {
        val firstPlayer = config.players.first()
        val secondaryPlayer = if (config.specialTurnRuleEnabled) {
            config.players[3 % config.players.size]
        } else null

        return Turn(
            number = 0,
            playerId = firstPlayer.id,
            secondaryPlayerId = secondaryPlayer?.id
        )
    }

    fun createNext(currentTurn: Turn, config: GameConfig): Turn {
        val nextNumber = currentTurn.number + 1
        val players = config.players
        val playerIndex = nextNumber % players.size
        val nextPlayer = players[playerIndex]
        val secondaryPlayer = if (config.specialTurnRuleEnabled) {
            players[(playerIndex + 3) % players.size]
        } else null

        return Turn(
            number = nextNumber,
            playerId = nextPlayer.id,
            secondaryPlayerId = secondaryPlayer?.id
        )
    }
}