package org.example.project.catan_companion_feature

import org.example.project.catan_companion_feature.domain.dataclass.*
import org.example.project.catan_companion_feature.domain.enums.*

fun makeTestPlayers(count: Int = 3): List<Player> =
    (1..count).map { makeTestPlayer(id = it.toLong(), name = "Player $it") }

fun makeTestPlayer(id: Long = -1L, name: String = "Andrzej") = Player(
    id = id,
    name = name
)

fun makeTestGamePlayers(count: Int = 3, gameId: Long = 1L): List<GamePlayer> =
    (1..count).map { i ->
        GamePlayer(
            gameId = gameId,
            playerId = i.toLong(),
            playerName = "Player $i",
            orderIndex = i - 1
        )
    }

fun makeTestGame(
    id: Long = 1L,
    turnDurationMillis: Long = 180_000L,
    expansions: Set<GameExpansion> = emptySet(),
    specialTurnRuleEnabled: Boolean = false,
    status: GameStatus = GameStatus.IN_PROGRESS,
    startedAt: Long = 1_000_000_500_100_900L,
    finishedAt: Long? = null,
    winnerId: Long? = null,
    players: List<GamePlayer> = makeTestGamePlayers(gameId = id)
): Game = Game(
    id = id,
    turnDurationMillis = turnDurationMillis,
    expansions = expansions,
    specialTurnRuleEnabled = specialTurnRuleEnabled,
    status = status,
    startedAt = startedAt,
    finishedAt = finishedAt,
    winnerId = winnerId,
    players = players
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
    players: List<GamePlayer> = makeTestGamePlayers()
): List<Turn> =
    (0 until count).map { index ->
        makeTestTurn(
            id = (index + 1).toLong(),
            number = index,
            playerId = players[index % players.size].playerId
        )
    }
