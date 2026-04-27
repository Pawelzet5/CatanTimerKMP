package io.github.pawelzielinski.catantimer.catanCompanion.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.FakeGameSessionCoordinator
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakeGameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakeTurnRepository
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay.GameplayAction
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay.GameplayPhase
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay.GameplayViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.service.HapticService
import io.github.pawelzielinski.catantimer.catanCompanion.testSessionWithMultipleTurns
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameplayViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GameplayViewModel init, session started, phase is DICE_SELECTION`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            assertEquals(GameplayPhase.DICE_SELECTION, viewModel.uiState.value.phase)
        }

    @Test
    fun `GameplayViewModel continueFromDice, dice sum is 7, phase transitions to EVENT`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.onAction(GameplayAction.DiceSelected(red = 3, yellow = 4, event = null))
            viewModel.onAction(GameplayAction.ContinueFromDiceClick)
            advanceUntilIdle()
            assertEquals(GameplayPhase.EVENT, viewModel.uiState.value.phase)
        }

    @Test
    fun `GameplayViewModel continueFromDice, dice sum is not 7, phase transitions to MAIN_TIMER`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.onAction(GameplayAction.DiceSelected(red = 2, yellow = 4, event = null))
            viewModel.onAction(GameplayAction.ContinueFromDiceClick)
            advanceUntilIdle()
            assertEquals(GameplayPhase.MAIN_TIMER, viewModel.uiState.value.phase)
        }

    @Test
    fun `GameplayViewModel navigateToPreviousTurn, multiple turns exist, isViewingLatest becomes false`() =
        runTest(testDispatcher) {
            val coordinator = FakeGameSessionCoordinator()
            coordinator.setSession(testSessionWithMultipleTurns())
            val viewModel = makeViewModel(coordinator = coordinator)
            advanceUntilIdle()
            viewModel.onAction(GameplayAction.PreviousClick)
            assertFalse(viewModel.uiState.value.isViewingLatest)
        }

    @Test
    fun `GameplayViewModel jumpToCurrentTurn, viewing historical turn, isViewingLatest restored`() =
        runTest(testDispatcher) {
            val coordinator = FakeGameSessionCoordinator()
            coordinator.setSession(testSessionWithMultipleTurns())
            val viewModel = makeViewModel(coordinator = coordinator)
            advanceUntilIdle()
            viewModel.onAction(GameplayAction.PreviousClick)
            viewModel.onAction(GameplayAction.JumpToCurrentClick)
            assertTrue(viewModel.uiState.value.isViewingLatest)
        }

    @Test
    fun `GameplayViewModel nextTurn, turn completed, phase resets to DICE_SELECTION`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.onAction(GameplayAction.DiceSelected(red = 2, yellow = 4, event = null))
            viewModel.onAction(GameplayAction.ContinueFromDiceClick)
            advanceUntilIdle()
            viewModel.onAction(GameplayAction.NextTurnClick)
            advanceUntilIdle()
            assertEquals(GameplayPhase.DICE_SELECTION, viewModel.uiState.value.phase)
        }

    private fun makeViewModel(
        coordinator: FakeGameSessionCoordinator = FakeGameSessionCoordinator()
    ) = GameplayViewModel(
        gameId = 1L,
        sessionCoordinator = coordinator,
        turnRepository = FakeTurnRepository(),
        gameRepository = FakeGameRepository(),
        hapticService = HapticService
    )
}
