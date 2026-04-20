package org.example.project.catan_companion_feature.domain

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.core.domain.DataError
import org.example.project.core.domain.IllegalOperationError
import org.example.project.core.domain.Result
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.catan_companion_feature.domain.session.GameSessionCoordinator
import org.example.project.catan_companion_feature.domain.session.DefaultGameSessionCoordinator
import org.example.project.catan_companion_feature.data.fakes.repository.FakeGameRepository
import org.example.project.catan_companion_feature.data.fakes.repository.FakeTurnRepository
import org.example.project.catan_companion_feature.makeTestGame
import org.example.project.catan_companion_feature.makeTestGamePlayers
import org.example.project.catan_companion_feature.makeTestTurn
import org.example.project.catan_companion_feature.makeTestTurns
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameSessionCoordinatorTest {

    private lateinit var fakeGameRepository: FakeGameRepository
    private lateinit var fakeTurnRepository: FakeTurnRepository
    private lateinit var coordinator: GameSessionCoordinator

    @BeforeTest
    fun setUp() {
        fakeGameRepository = FakeGameRepository()
        fakeTurnRepository = FakeTurnRepository()
        coordinator = DefaultGameSessionCoordinator(
            gameRepository = fakeGameRepository,
            turnRepository = fakeTurnRepository
        )
    }

    // region startSession

    @Test
    fun `startSession, new game with no turns, returns success and initializes session`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)

        // WHEN
        val result = coordinator.startSession(gameId = game.id)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        assertNotNull(coordinator.currentSession.value)
        assertEquals(game, coordinator.currentSession.value!!.game)
    }

    @Test
    fun `startSession, game has no turns, creates and persists initial turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        assertEquals(1, fakeTurnRepository.allTurns.size)
        assertEquals(0, fakeTurnRepository.allTurns.first().number)
        assertEquals(game.players.first().playerId, fakeTurnRepository.allTurns.first().playerId)
    }

    @Test
    fun `startSession, specialTurnRule enabled, sets secondaryPlayerId on initial turn`() = runTest {
        // GIVEN
        val players = makeTestGamePlayers(count = 5)
        val game = makeTestGame(specialTurnRuleEnabled = true, players = players)
        fakeGameRepository.seedGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        // turn 0: playerIndex = 0 % 5 = 0, secondary = (0 + 3) % 5 = 3 → players[3]
        val initialTurn = fakeTurnRepository.allTurns.first()
        assertEquals(players[3].playerId, initialTurn.secondaryPlayerId)
    }

    @Test
    fun `startSession, specialTurnRule disabled, sets null secondaryPlayerId`() = runTest {
        // GIVEN
        val players = makeTestGamePlayers(count = 5)
        val game = makeTestGame(specialTurnRuleEnabled = false, players = players)
        fakeGameRepository.seedGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        assertNull(fakeTurnRepository.allTurns.first().secondaryPlayerId)
    }

    @Test
    fun `startSession, new game, sets selectedTurn to initial turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(0, session.selectedTurn.number)
        assertEquals(session.latestTurn, session.selectedTurn)
    }

    @Test
    fun `startSession, existing game with turns, sets selectedTurn to last turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 3, players = game.players)
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.seedTurns(gameId = game.id, *turns.toTypedArray())

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(turns.last(), session.selectedTurn)
        assertEquals(turns.last(), session.latestTurn)
    }

    @Test
    fun `startSession, game has multiple turns, sets recentTurns excluding active turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 4, players = game.players)
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.seedTurns(gameId = game.id, *turns.toTypedArray())

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(3, session.recentTurns.size)
        assertTrue(session.recentTurns.none { it.number == turns.last().number })
    }

    @Test
    fun `startSession, game has more than 3 prior turns, limits recentTurns to 3`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 6, players = game.players)
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.seedTurns(gameId = game.id, *turns.toTypedArray())

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(3, session.recentTurns.size)
    }

    @Test
    fun `startSession, game has only one existing turn, recentTurns is empty`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val singleTurn = makeTestTurn(id = 1L, number = 0)
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.seedTurns(gameId = game.id, singleTurn)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertTrue(session.recentTurns.isEmpty())
    }

    @Test
    fun `startSession, previous session exists, clears it before initializing new one`() = runTest {
        // GIVEN
        val firstGame = makeTestGame(id = 1L)
        val secondGame = makeTestGame(id = 2L)
        fakeGameRepository.seedGame(firstGame)
        fakeGameRepository.seedGame(secondGame)
        coordinator.startSession(gameId = firstGame.id)

        // WHEN
        coordinator.startSession(gameId = secondGame.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(secondGame.id, session.game.id)
    }

    @Test
    fun `startSession, game exists, emits a session`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)


        coordinator.currentSession.test {
            assertNull(awaitItem())

            // WHEN
            coordinator.startSession(gameId = game.id)


            // THEN
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `startSession, game not found, returns failure and leaves session null`() = runTest {
        // GIVEN
        fakeGameRepository.shouldFailOnGetGame = true

        // WHEN
        val result = coordinator.startSession(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNull(coordinator.currentSession.value)
    }

    @Test
    fun `startSession, getTurnsForGame fails, returns failure`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.shouldFailOnGetAll = true

        // WHEN
        val result = coordinator.startSession(gameId = game.id)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNull(coordinator.currentSession.value)
    }

    @Test
    fun `startSession, addTurn fails for new game, returns failure`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.shouldFailOnAdd = true

        // WHEN
        val result = coordinator.startSession(gameId = game.id)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNull(coordinator.currentSession.value)
    }

    // endregion

    // region finishSession

    @Test
    fun `finishSession, active session exists, marks game as finished and clears session`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        val result = coordinator.finishSession(finishedAt = 30_000L, winnerId = null)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        assertNull(coordinator.currentSession.value)
        assertTrue(fakeGameRepository.games.first().status == GameStatus.COMPLETED)
    }

    @Test
    fun `finishSession, active session exists, emits null session`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)

        coordinator.currentSession.test {
            awaitItem()
            coordinator.startSession(gameId = game.id)
            awaitItem()

            // WHEN
            coordinator.finishSession(finishedAt = 30_000L, winnerId = null)

            // THEN
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `finishSession, no active session, returns failure`() = runTest {
        // WHEN
        val result = coordinator.finishSession(finishedAt = 30_000L, winnerId = null)

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    @Test
    fun `finishSession, winnerId provided, persists winnerId`() = runTest {
        // GIVEN
        val players = makeTestGamePlayers(count = 3)
        val game = makeTestGame(players = players)
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        val winnerId = players[1].playerId

        // WHEN
        coordinator.finishSession(finishedAt = 30_000L, winnerId = winnerId)

        // THEN
        assertEquals(winnerId, fakeGameRepository.games.first().winnerId)
    }

    @Test
    fun `finishSession, repository fails, returns failure and preserves session`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        fakeGameRepository.shouldFailOnFinishGame = true

        // WHEN
        val result = coordinator.finishSession(finishedAt = 30_000L, winnerId = null)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNotNull(coordinator.currentSession.value)
    }

    // endregion

    // region completeTurn

    @Test
    fun `completeTurn, active turn selected, persists completed turn with duration`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        val duration = 60_000L

        // WHEN
        val result = coordinator.completeTurn(durationMillis = duration)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        assertEquals(duration, fakeTurnRepository.allTurns.first { it.number == 0 }.durationMillis)
    }

    @Test
    fun `completeTurn, active turn selected, creates and persists next turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertEquals(2, fakeTurnRepository.allTurns.size)
        assertEquals(1, fakeTurnRepository.allTurns.last().number)
    }

    @Test
    fun `completeTurn, specialTurnRule enabled, sets secondaryPlayerId on next turn`() = runTest {
        // GIVEN
        val players = makeTestGamePlayers(count = 5)
        val game = makeTestGame(specialTurnRuleEnabled = true, players = players)
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        // turn 1: playerIndex = 1 % 5 = 1, secondary = (1 + 3) % 5 = 4 → players[4]
        val nextTurn = fakeTurnRepository.allTurns.last()
        assertEquals(players[4].playerId, nextTurn.secondaryPlayerId)
    }

    @Test
    fun `completeTurn, specialTurnRule disabled, sets null secondaryPlayerId on next turn`() = runTest {
        // GIVEN
        val players = makeTestGamePlayers(count = 5)
        val game = makeTestGame(specialTurnRuleEnabled = false, players = players)
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertNull(fakeTurnRepository.allTurns.last().secondaryPlayerId)
    }

    @Test
    fun `completeTurn, active turn selected, updates selectedTurn and latestTurn to next turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(1, session.selectedTurn.number)
        assertEquals(session.latestTurn, session.selectedTurn)
    }

    @Test
    fun `completeTurn, multiple players, rotates players correctly`() = runTest {
        // GIVEN
        val players = makeTestGamePlayers(count = 3)
        val game = makeTestGame(players = players)
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        repeat(3) { coordinator.completeTurn(durationMillis = 0L) }

        // THEN
        // initial turn 0, after 3 completions we're on turn 3: 3 % 3 = 0 → players[0]
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(players[0].playerId, session.selectedTurn.playerId)
    }

    @Test
    fun `completeTurn, recentTurns at limit, appends and drops oldest`() = runTest {
        // GIVEN – restore a game that already has 4 turns (turns 0, 1, 2, 3)
        // so recentTurns = [1, 2, 3], selectedTurn = turn3 (limit already reached)
        val game = makeTestGame()
        val existingTurns = makeTestTurns(count = 4, players = game.players)
        fakeGameRepository.seedGame(game)
        fakeTurnRepository.seedTurns(gameId = game.id, *existingTurns.toTypedArray())
        coordinator.startSession(gameId = game.id)

        // WHEN – complete the active turn (turn 3)
        coordinator.completeTurn(durationMillis = 30_000L)

        // THEN – recentTurns = [1, 2, 3] + turn3 → takeLast(3) = [2, 3, turn3(completed)]
        // turn1 drops off, newly-created turn4 is selectedTurn and not in recentTurns
        val sessionAfter = coordinator.currentSession.value!!
        assertEquals(3, sessionAfter.recentTurns.size)
        assertEquals(existingTurns[1].number, sessionAfter.recentTurns[0].number)
        assertEquals(existingTurns[2].number, sessionAfter.recentTurns[1].number)
        assertEquals(existingTurns[3].number, sessionAfter.recentTurns[2].number)
        assertEquals(4, sessionAfter.selectedTurn.number)
        assertTrue(sessionAfter.recentTurns.none { it.number == sessionAfter.selectedTurn.number })
    }

    @Test
    fun `completeTurn, called repeatedly, keeps recentTurns within limit`() = runTest {
        // GIVEN – fresh game, no prior turns
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN – complete 5 turns
        repeat(5) { coordinator.completeTurn(durationMillis = 10_000L) }

        // THEN – regardless of how many turns exist in total, recentTurns never exceeds 3
        val session = coordinator.currentSession.value!!
        assertTrue(session.recentTurns.size <= 3)
        // The 3 most recent completed turns should be turns 3, 4, 5 (the active turn is 5)
        // active turn is turn 5, so recentTurns should be turns [2, 3, 4]
        assertEquals(listOf(2, 3, 4), session.recentTurns.map { it.number })
    }

    @Test
    fun `completeTurn, updateTurn fails, returns failure`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        fakeTurnRepository.shouldFailOnUpdate = true

        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    @Test
    fun `completeTurn, addTurn fails, returns failure`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        fakeTurnRepository.shouldFailOnAdd = true

        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    @Test
    fun `completeTurn, no active session, returns failure`() = runTest {
        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    // endregion

    // region updateSelectedTurnDice

    @Test
    fun `updateSelectedTurnDice, active turn selected, updates dice in memory and persists`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.updateSelectedTurnDice(redDice = 3, yellowDice = 5, eventDice = null)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(3, session.selectedTurn.redDice)
        assertEquals(5, session.selectedTurn.yellowDice)
        assertNull(session.selectedTurn.eventDice)
    }

    @Test
    fun `updateSelectedTurnDice, eventDice provided, persists eventDice`() = runTest {
        // GIVEN
        val game = makeTestGame(expansions = setOf(GameExpansion.CITIES_AND_KNIGHTS))
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.updateSelectedTurnDice(redDice = 2, yellowDice = 4, eventDice = EventDiceType.TRADE)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(EventDiceType.TRADE, session.selectedTurn.eventDice)
    }

    @Test
    fun `updateSelectedTurnDice, repository fails, does not update memory state`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        val originalTurn = coordinator.currentSession.value!!.selectedTurn
        fakeTurnRepository.shouldFailOnUpdate = true

        // WHEN
        val result = coordinator.updateSelectedTurnDice(redDice = 3, yellowDice = 5, eventDice = null)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(originalTurn, session.selectedTurn)
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    @Test
    fun `updateSelectedTurnDice, no active session, returns failure`() = runTest {
        // WHEN
        val result = coordinator.updateSelectedTurnDice(redDice = 3, yellowDice = 5, eventDice = null)

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    // endregion

    // region updateSelectedTurnDuration

    @Test
    fun `updateSelectedTurnDuration, active turn selected, updates duration in memory and persists`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        val duration = 120_000L

        // WHEN
        coordinator.updateSelectedTurnDuration(durationMillis = duration)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(duration, session.selectedTurn.durationMillis)
        assertEquals(duration, fakeTurnRepository.allTurns.first().durationMillis)
    }

    @Test
    fun `updateSelectedTurnDuration, repository fails, does not update memory state`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.seedGame(game)
        coordinator.startSession(gameId = game.id)
        val originalDuration = coordinator.currentSession.value!!.selectedTurn.durationMillis
        fakeTurnRepository.shouldFailOnUpdate = true

        // WHEN
        val result = coordinator.updateSelectedTurnDuration(durationMillis = 120_000L)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(originalDuration, session.selectedTurn.durationMillis)
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    @Test
    fun `updateSelectedTurnDuration, no active session, returns failure`() = runTest {
        // WHEN
        val result = coordinator.updateSelectedTurnDuration(durationMillis = 120_000L)

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    // endregion
}
