package io.github.pawelzielinski.catantimer.catanCompanion.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakeGameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakePlayerRepository
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard.DashboardViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.testGame
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

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
    fun `DashboardViewModel init, no action taken, resumableGame is null and not loading`() =
        runTest(testDispatcher) {
            val viewModel = DashboardViewModel(FakeGameRepository(), FakePlayerRepository())
            assertNull(viewModel.uiState.value.resumableGame)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `DashboardViewModel load, in-progress game exists, resumableGame present in state`() =
        runTest(testDispatcher) {
            val repo = FakeGameRepository()
            repo.setInProgressGame(testGame())
            val viewModel = DashboardViewModel(repo, FakePlayerRepository())
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.resumableGame)
        }

    @Test
    fun `DashboardViewModel load, no in-progress games, resumableGame is null`() =
        runTest(testDispatcher) {
            val viewModel = DashboardViewModel(FakeGameRepository(), FakePlayerRepository())
            advanceUntilIdle()
            assertNull(viewModel.uiState.value.resumableGame)
        }
}
