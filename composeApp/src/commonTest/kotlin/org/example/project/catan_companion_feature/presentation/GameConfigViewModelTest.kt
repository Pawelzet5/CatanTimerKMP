package org.example.project.catan_companion_feature.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.catan_companion_feature.data.fakes.repository.FakeGameRepository
import org.example.project.catan_companion_feature.data.fakes.repository.FakePlayerRepository
import org.example.project.catan_companion_feature.domain.usecase.CreateGameUseCase
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigAction
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigEvent
import org.example.project.catan_companion_feature.presentation.gameconfig.GameConfigViewModel
import org.example.project.catan_companion_feature.testPlayer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameConfigViewModelTest {

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
    fun `GameConfig validation, fewer than 3 players selected, isValid is false`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(1L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(2L)))
            assertFalse(viewModel.uiState.value.isValid)
        }

    @Test
    fun `GameConfig validation, 3 players selected, isValid is true`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.onAction(GameConfigAction.PlayerCountSelected(3))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(1L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(2L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(3L)))
            assertTrue(viewModel.uiState.value.isValid)
        }

    @Test
    fun `GameConfig specialTurnRule toggle, fewer than 5 players selected, isValid is false`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.onAction(GameConfigAction.SpecialTurnRuleToggled)
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(1L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(2L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(3L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(4L)))
            assertFalse(viewModel.uiState.value.isValid)
        }

    @Test
    fun `GameConfig start game, valid config, NavigateToGameplay event emitted`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            val events = mutableListOf<GameConfigEvent>()
            val job = launch { viewModel.events.collect { events.add(it) } }
            viewModel.onAction(GameConfigAction.PlayerCountSelected(3))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(1L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(2L)))
            viewModel.onAction(GameConfigAction.PlayerToggled(testPlayer(3L)))
            viewModel.onAction(GameConfigAction.StartGameClick)
            advanceUntilIdle()
            assertTrue(events.any { it is GameConfigEvent.NavigateToGameplay })
            job.cancel()
        }

    private fun makeViewModel() = GameConfigViewModel(
        playerRepository = FakePlayerRepository(),
        createGameUseCase = CreateGameUseCase(FakeGameRepository())
    )
}
