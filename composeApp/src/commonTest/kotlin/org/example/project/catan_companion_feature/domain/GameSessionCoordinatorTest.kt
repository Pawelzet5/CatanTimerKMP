package org.example.project.catan_companion_feature.domain

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.example.project.core.util.DataError
import org.example.project.core.util.IllegalOperationError
import org.example.project.core.util.Result
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.repository.FakeGameRepository
import org.example.project.catan_companion_feature.domain.repository.FakeTurnRepository
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
        coordinator = GameSessionCoordinator(
            gameRepository = fakeGameRepository,
            turnRepository = fakeTurnRepository
        )
    }

    // region startSession

    @Test
    fun `startSession returns success and initializes session for new game`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)

        // WHEN
        val result = coordinator.startSession(gameId = game.id)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        assertNotNull(coordinator.currentSession.value)
        assertEquals(game, coordinator.currentSession.value!!.game)
    }

    @Test
    fun `startSession creates and persists initial turn when game has no turns`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        assertEquals(1, fakeTurnRepository.turns.size)
        assertEquals(0, fakeTurnRepository.turns.first().number)
        assertEquals(game.config.players.first().id, fakeTurnRepository.turns.first().playerId)
    }

    @Test
    fun `startSession sets secondaryPlayerId on initial turn when specialTurnRule enabled`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 5)
        val config = makeTestGameConfig(specialTurnRuleEnabled = true, players = players)
        val game = makeTestGame(config = config)
        fakeGameRepository.addGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        // turn 0: playerIndex = 0 % 5 = 0, secondary = (0 + 3) % 5 = 3 → players[3]
        val initialTurn = fakeTurnRepository.turns.first()
        assertEquals(players[3].id, initialTurn.secondaryPlayerId)
    }

    @Test
    fun `startSession sets null secondaryPlayerId when specialTurnRule disabled`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        assertNull(fakeTurnRepository.turns.first().secondaryPlayerId)
    }

    @Test
    fun `startSession sets selectedTurn to initial turn for new game`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(0, session.selectedTurn.number)
    }

    @Test
    fun `startSession sets selectedTurn to last turn when restoring existing game`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 3, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(turns.last(), session.selectedTurn)
    }

    @Test
    fun `startSession sets recentTurns to previous turns excluding active turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 4, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(3, session.recentTurns.size)
        assertTrue(session.recentTurns.none { it.number == turns.last().number })
    }

    @Test
    fun `startSession limits recentTurns to 3 even when more turns exist`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 6, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(3, session.recentTurns.size)
    }

    @Test
    fun `startSession sets empty recentTurns when game has only one existing turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val singleTurn = makeTestTurn(id = 1L, number = 0)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(singleTurn)

        // WHEN
        coordinator.startSession(gameId = game.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertTrue(session.recentTurns.isEmpty())
    }

    @Test
    fun `startSession clears previous session before initializing new one`() = runTest {
        // GIVEN
        val firstGame = makeTestGame(id = 1L)
        val secondGame = makeTestGame(id = 2L)
        fakeGameRepository.addGame(firstGame)
        fakeGameRepository.addGame(secondGame)
        coordinator.startSession(gameId = firstGame.id)

        // WHEN
        coordinator.startSession(gameId = secondGame.id)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(secondGame.id, session.game.id)
    }

    @Test
    fun `startSession emits a session after calling`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)


        coordinator.currentSession.test {
            assertNull(awaitItem())

            // WHEN
            coordinator.startSession(gameId = game.id)


            coordinator.finishSession()


            // THEN
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `startSession returns failure and leaves session null when game not found`() = runTest {
        // GIVEN
        fakeGameRepository.shouldFailOnGetGame = true

        // WHEN
        val result = coordinator.startSession(gameId = 1L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNull(coordinator.currentSession.value)
    }

    @Test
    fun `startSession returns failure when getTurnsForGame fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        fakeTurnRepository.shouldFailOnGetAll = true

        // WHEN
        val result = coordinator.startSession(gameId = game.id)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNull(coordinator.currentSession.value)
    }

    @Test
    fun `startSession returns failure when addTurn fails for new game`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
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
    fun `finishSession marks game as finished and clears session`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        val result = coordinator.finishSession()

        // THEN
        assertIs<Result.Success<Unit>>(result)
        assertNull(coordinator.currentSession.value)
    }

    @Test
    fun `finishSession emits null session after clearing`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)

        coordinator.currentSession.test {
            assertNull(awaitItem())

            coordinator.startSession(gameId = game.id)
            assertNotNull(awaitItem())

            // WHEN
            coordinator.finishSession()

            // THEN
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `finishSession returns failure when no active session exists`() = runTest {
        // WHEN
        val result = coordinator.finishSession()

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    @Test
    fun `finishSession returns failure and preserves session when repository fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        fakeGameRepository.shouldFailOnFinishGame = true

        // WHEN
        val result = coordinator.finishSession()

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertNotNull(coordinator.currentSession.value)
    }

    // endregion

    // region completeTurn

    @Test
    fun `completeTurn returns success and persists completed turn with duration`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        val duration = 60_000L

        // WHEN
        val result = coordinator.completeTurn(durationMillis = duration)

        // THEN
        assertIs<Result.Success<Unit>>(result)
        assertEquals(duration, fakeTurnRepository.turns.first { it.number == 0 }.durationMillis)
    }

    @Test
    fun `completeTurn creates and persists next turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertEquals(2, fakeTurnRepository.turns.size)
        assertEquals(1, fakeTurnRepository.turns.last().number)
    }

    @Test
    fun `completeTurn sets secondaryPlayerId on next turn when specialTurnRule enabled`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 5)
        val config = makeTestGameConfig(specialTurnRuleEnabled = true, players = players)
        val game = makeTestGame(config = config)
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        // turn 1: playerIndex = 1 % 5 = 1, secondary = (1 + 3) % 5 = 4 → players[4]
        val nextTurn = fakeTurnRepository.turns.last()
        assertEquals(players[4].id, nextTurn.secondaryPlayerId)
    }

    @Test
    fun `completeTurn sets null secondaryPlayerId when specialTurnRule disabled`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertNull(fakeTurnRepository.turns.last().secondaryPlayerId)
    }

    @Test
    fun `completeTurn updates selectedTurn to next turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(1, session.selectedTurn.number)
    }

    @Test
    fun `completeTurn rotates players correctly across multiple turns`() = runTest {
        // GIVEN
        val players = makeTestPlayers(count = 3)
        val game = makeTestGame(config = makeTestGameConfig(players = players))
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        repeat(3) { coordinator.completeTurn(durationMillis = 0L) }

        // THEN
        // initial turn 0, after 3 completions we're on turn 3: 3 % 3 = 0 → players[0]
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(players[0].id, session.selectedTurn.playerId)
    }

    @Test
    fun `completeTurn appends completed turn to recentTurns and respects limit`() = runTest {
        // GIVEN – restore a game that already has 3 turns (turns 0, 1, 2)
        // so recentTurns = [turn0, turn1], selectedTurn = turn2
        val game = makeTestGame()
        val existingTurns = makeTestTurns(count = 3, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*existingTurns.toTypedArray())
        coordinator.startSession(gameId = game.id)

        // Sanity-check initial state
        val sessionBefore = coordinator.currentSession.value!!
        assertEquals(2, sessionBefore.recentTurns.size)
        assertEquals(existingTurns[0].number, sessionBefore.recentTurns[0].number)
        assertEquals(existingTurns[1].number, sessionBefore.recentTurns[1].number)

        // WHEN – complete the active turn (turn 2)
        coordinator.completeTurn(durationMillis = 30_000L)

        // THEN – recentTurns should now be [turn1, turn2(completed)], still capped at 3
        // turn0 drops off because RECENT_TURNS_LIMIT = 3 and we now have exactly 3 candidates
        val sessionAfter = coordinator.currentSession.value!!
        assertEquals(3, sessionAfter.recentTurns.size)
        assertEquals(existingTurns[1].number, sessionAfter.recentTurns[0].number)
        assertEquals(existingTurns[2].number, sessionAfter.recentTurns[1].number)
        // The newly-created turn 3 is selectedTurn, not in recentTurns
        assertEquals(3, sessionAfter.selectedTurn.number)
        assertTrue(sessionAfter.recentTurns.none { it.number == sessionAfter.selectedTurn.number })
    }

    @Test
    fun `completeTurn keeps recentTurns within limit when called repeatedly`() = runTest {
        // GIVEN – fresh game, no prior turns
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
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
    fun `completeTurn returns IllegalOperationError when viewing historical turn`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 2, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())
        coordinator.startSession(gameId = game.id)
        coordinator.selectTurn(turns.first())

        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<IllegalOperationError>>(result)
        assertEquals(IllegalOperationError, (result as Result.Failure).error)
    }

    @Test
    fun `completeTurn returns failure when updateTurn fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        fakeTurnRepository.shouldFailOnUpdate = true

        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    @Test
    fun `completeTurn returns failure when addTurn fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        fakeTurnRepository.shouldFailOnAdd = true

        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    @Test
    fun `completeTurn returns failure when no active session exists`() = runTest {
        // WHEN
        val result = coordinator.completeTurn(durationMillis = 60_000L)

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    // endregion

    // region selectTurn

    @Test
    fun `selectTurn updates selectedTurn in memory`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 2, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.selectTurn(turns.first())

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(turns.first(), session.selectedTurn)
    }

    @Test
    fun `selectTurn does not call any repository operation`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 2, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())
        coordinator.startSession(gameId = game.id)
        val turnsCountBefore = fakeTurnRepository.turns.size

        // WHEN
        coordinator.selectTurn(turns.first())

        // THEN
        assertEquals(turnsCountBefore, fakeTurnRepository.turns.size)
    }

    // endregion

    // region selectActiveTurn

    @Test
    fun `selectActiveTurn restores selectedTurn to last turn in db`() = runTest {
        // GIVEN
        val game = makeTestGame()
        val turns = makeTestTurns(count = 3, players = game.config.players)
        fakeGameRepository.addGame(game)
        fakeTurnRepository.addTurns(*turns.toTypedArray())
        coordinator.startSession(gameId = game.id)
        coordinator.selectTurn(turns.first())

        // WHEN
        coordinator.selectActiveTurn()

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(turns.last(), session.selectedTurn)
    }

    @Test
    fun `selectActiveTurn returns failure when no active session exists`() = runTest {
        // WHEN
        val result = coordinator.selectActiveTurn()

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    @Test
    fun `selectActiveTurn returns failure when getLastTurn fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        fakeTurnRepository.shouldFailOnGetLast = true

        // WHEN
        val result = coordinator.selectActiveTurn()

        // THEN
        assertIs<Result.Failure<DataError.Local>>(result)
    }

    // endregion

    // region updateSelectedTurnDice

    @Test
    fun `updateSelectedTurnDice updates dice in memory and persists`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
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
    fun `updateSelectedTurnDice persists eventDice for cities and knights expansion`() = runTest {
        // GIVEN
        val config = makeTestGameConfig(expansions = setOf(GameExpansion.CITIES_AND_KNIGHTS))
        val game = makeTestGame(config = config)
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)

        // WHEN
        coordinator.updateSelectedTurnDice(redDice = 2, yellowDice = 4, eventDice = EventDiceType.TRADE)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(EventDiceType.TRADE, session.selectedTurn.eventDice)
    }

    @Test
    fun `updateSelectedTurnDice does not update memory state when repository fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        val originalTurn = coordinator.currentSession.value!!.selectedTurn
        fakeTurnRepository.shouldFailOnUpdate = true

        // WHEN
        val result = coordinator.updateSelectedTurnDice(redDice = 3, yellowDice = 5, eventDice = null)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(originalTurn, session.selectedTurn)
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    @Test
    fun `updateSelectedTurnDice returns failure when no active session exists`() = runTest {
        // WHEN
        val result = coordinator.updateSelectedTurnDice(redDice = 3, yellowDice = 5, eventDice = null)

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    // endregion

    // region updateSelectedTurnDuration

    @Test
    fun `updateSelectedTurnDuration updates duration in memory and persists`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        val duration = 120_000L

        // WHEN
        coordinator.updateSelectedTurnDuration(durationMillis = duration)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(duration, session.selectedTurn.durationMillis)
        assertEquals(duration, fakeTurnRepository.turns.first().durationMillis)
    }

    @Test
    fun `updateSelectedTurnDuration does not update memory state when repository fails`() = runTest {
        // GIVEN
        val game = makeTestGame()
        fakeGameRepository.addGame(game)
        coordinator.startSession(gameId = game.id)
        val originalDuration = coordinator.currentSession.value!!.selectedTurn.durationMillis
        fakeTurnRepository.shouldFailOnUpdate = true

        // WHEN
        val result = coordinator.updateSelectedTurnDuration(durationMillis = 120_000L)

        // THEN
        val session = coordinator.currentSession.value
        assertNotNull(session)
        assertEquals(originalDuration, session.selectedTurn.durationMillis)
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    @Test
    fun `updateSelectedTurnDuration returns failure when no active session exists`() = runTest {
        // WHEN
        val result = coordinator.updateSelectedTurnDuration(durationMillis = 120_000L)

        // THEN
        assertEquals(DataError.Local.NOT_FOUND, (result as Result.Failure).error)
    }

    // endregion
}