package org.example.project.catan_companion_feature

import org.example.project.catan_companion_feature.domain.dataclass.*
import org.example.project.catan_companion_feature.domain.enums.*

fun makeTestPlayers(count: Int = 3): List<Player> =
    (1..count).map { makeTestPlayer(id = it.toLong(), name = "Player $it") }

fun makeTestPlayer(id: Long = -1L, name: String = "Andrzej") = Player(
    id = id,
    name = name
)

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
    status: GameStatus = GameStatus.ACTIVE,
    startedAt: Long = 1000000500100900,
    finishedAt: Long? = null
): Game = Game(
    id = id,
    config = config,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt
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