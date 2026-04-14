package org.example.project.catan_companion_feature.domain

import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GameConfig
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus

fun makeTestPlayers(count: Int = 3): List<Player> =
    (1..count).map { Player(id = it.toLong(), name = "Player $it") }

fun makeTestGameConfig(
    turnDurationMillis: Long = 180_000L,
    expansions: Set<GameExpansion> = emptySet(),
    specialTurnRuleEnabled: Boolean = false,
    players: List<Player> = makeTestPlayers()
): GameConfig = GameConfig(
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    players = players
)

fun makeTestGame(
    id: Long = 1L,
    config: GameConfig = makeTestGameConfig(),
    status: GameStatus = GameStatus.IN_PROGRESS
): Game = Game(
    id = id,
    config = config,
    status = status
)

fun makeTestTurn(
    id: Long = 1L,
    number: Int = 0,
    playerId: Long = 1L,
    redDice: Int? = null,
    yellowDice: Int? = null,
    eventDice: EventDiceType? = null,
    durationMillis: Long = 0L
): Turn = Turn(
    id = id,
    number = number,
    playerId = playerId,
    redDice = redDice,
    yellowDice = yellowDice,
    eventDice = eventDice,
    durationMillis = durationMillis
)

fun makeTestTurns(
    count: Int,
    players: List<Player> = makeTestPlayers()
): List<Turn> =
    (0 until count).map { index ->
        makeTestTurn(
            id = (index + 1).toLong(),
            number = index,
            playerId = players[index % players.size].id
        )
    }