package org.example.project.catan_companion_feature

import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.GamePlayer
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.dataclass.Turn
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.catan_companion_feature.domain.session.GameSession

fun testGame(id: Long = 1L) = Game(
    id = id,
    turnDurationMillis = 120_000L,
    expansions = emptySet(),
    specialTurnRuleEnabled = false,
    status = GameStatus.IN_PROGRESS,
    startedAt = 0L,
    players = listOf(
        GamePlayer(gameId = id, playerId = 1L, playerName = "Alice", orderIndex = 0),
        GamePlayer(gameId = id, playerId = 2L, playerName = "Bob", orderIndex = 1),
        GamePlayer(gameId = id, playerId = 3L, playerName = "Charlie", orderIndex = 2)
    )
)

fun testPlayer(id: Long = 1L, name: String = "Player $id") = Player(
    id = id,
    name = name
)

fun testTurn(id: Long = 1L, gameId: Long = 1L, number: Int = 0) = Turn(
    id = id,
    gameId = gameId,
    number = number,
    playerId = 1L,
    playerName = "Alice"
)

fun testTurnWithDice(red: Int, yellow: Int, gameId: Long = 1L) = Turn(
    id = 1L,
    gameId = gameId,
    number = 0,
    playerId = 1L,
    playerName = "Alice",
    redDice = red,
    yellowDice = yellow
)

fun testSessionWithMultipleTurns(): GameSession {
    val game = testGame()
    val turn0 = testTurn(id = 1L, number = 0)
    val turn1 = testTurn(id = 2L, number = 1)
    val turn2 = testTurn(id = 3L, number = 2)
    return GameSession(
        game = game,
        latestTurn = turn2,
        selectedTurn = turn2,
        recentTurns = listOf(turn0, turn1)
    )
}
