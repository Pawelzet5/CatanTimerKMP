package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.github.pawelzielinski.catantimer.catan_companion_feature.data.fakes.repository.FakeGameRepository
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameStatus
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist.GamesListAction
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist.GamesListEvent
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameslist.GamesListViewModel
import io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.service.HapticService
import io.github.pawelzielinski.catantimer.catan_companion_feature.testGame
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GamesListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(repo: FakeGameRepository = FakeGameRepository()) =
        GamesListViewModel(repo, HapticService)

    @Test
    fun `ViewModel init, in-progress and completed games exist, state populated with both lists`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            repo.seedGame(testGame(id = 1L))
            repo.seedGame(testGame(id = 2L).copy(status = GameStatus.COMPLETED))
            val viewModel = makeViewModel(repo)
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(1, state.inProgressGames.size)
                assertEquals(1, state.completedGames.size)
            }
        }

    @Test
    fun `ViewModel init, no games exist, state has empty lists`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.inProgressGames.isEmpty())
                assertTrue(state.completedGames.isEmpty())
            }
        }

    @Test
    fun `BackClick action, any state, NavigateBack event emitted`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.events.test {
                viewModel.onAction(GamesListAction.BackClick)
                assertEquals(GamesListEvent.NavigateBack, awaitItem())
            }
        }

    @Test
    fun `GameClick action, clicked game is in-progress, NavigateToGameplay event emitted`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            repo.seedGame(testGame(id = 1L))
            val viewModel = makeViewModel(repo)
            viewModel.events.test {
                viewModel.onAction(GamesListAction.GameClick(gameId = 1L))
                assertEquals(GamesListEvent.NavigateToGameplay(1L), awaitItem())
            }
        }

    @Test
    fun `GameClick action, clicked game is completed, NavigateToGameSummary event emitted`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            repo.seedGame(testGame(id = 2L).copy(status = GameStatus.COMPLETED))
            val viewModel = makeViewModel(repo)
            viewModel.events.test {
                viewModel.onAction(GamesListAction.GameClick(gameId = 2L))
                assertEquals(GamesListEvent.NavigateToGameSummary(2L), awaitItem())
            }
        }

    @Test
    fun `GameClick action, game id not found in state, NavigateToGameSummary event emitted`() =
        runTest(testDispatcher) {
            val viewModel = makeViewModel()
            viewModel.events.test {
                viewModel.onAction(GamesListAction.GameClick(gameId = 999L))
                assertEquals(GamesListEvent.NavigateToGameSummary(999L), awaitItem())
            }
        }

    @Test
    fun `RequestDeleteGame action, any game, gameToDelete set in state`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            val game = testGame(id = 1L)
            repo.seedGame(game)
            val viewModel = makeViewModel(repo)
            viewModel.uiState.test {
                awaitItem() // initial emission
                viewModel.onAction(GamesListAction.RequestDeleteGame(game))
                assertEquals(game, awaitItem().gameToDelete)
            }
        }

    @Test
    fun `ConfirmDeleteGame action, gameToDelete is set, game deleted and gameToDelete cleared`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            val game = testGame(id = 1L)
            repo.seedGame(game)
            val viewModel = makeViewModel(repo)
            viewModel.uiState.test {
                awaitItem() // initial emission
                viewModel.onAction(GamesListAction.RequestDeleteGame(game))
                awaitItem() // gameToDelete set
                viewModel.onAction(GamesListAction.ConfirmDeleteGame)
                awaitItem() // intermediate: gameToDelete cleared, game still in list
                val state = awaitItem() // repo update propagates: game removed
                assertNull(state.gameToDelete)
                assertTrue(state.inProgressGames.isEmpty())
            }
        }

    @Test
    fun `ConfirmDeleteGame action, gameToDelete is null, game list unchanged`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            repo.seedGame(testGame(id = 1L))
            val viewModel = makeViewModel(repo)
            viewModel.uiState.test {
                val state = awaitItem()
                viewModel.onAction(GamesListAction.ConfirmDeleteGame)
                expectNoEvents()
                assertNull(state.gameToDelete)
                assertEquals(1, state.inProgressGames.size)
            }
        }

    @Test
    fun `DismissDeleteGame action, gameToDelete is set, gameToDelete cleared`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            val game = testGame(id = 1L)
            repo.seedGame(game)
            val viewModel = makeViewModel(repo)
            viewModel.uiState.test {
                awaitItem() // initial emission
                viewModel.onAction(GamesListAction.RequestDeleteGame(game))
                awaitItem() // gameToDelete set
                viewModel.onAction(GamesListAction.DismissDeleteGame)
                assertNull(awaitItem().gameToDelete)
            }
        }
}
