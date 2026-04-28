package io.github.pawelzielinski.catantimer.catanCompanion.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakeGameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.data.fakes.repository.FakeTurnRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.GetGameStatisticsUseCase
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary.GameSummaryViewModel
import io.github.pawelzielinski.catantimer.catanCompanion.testGame
import io.github.pawelzielinski.catantimer.catanCompanion.testTurnWithDice
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class GameSummaryViewModelTest {

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
    fun `GameSummaryViewModel init, valid game and turns exist, statistics loaded and isLoading false`() =
        runTest(testDispatcher) {
            val gameRepo = FakeGameRepository()
            gameRepo.setGame(testGame(id = 1L))
            val turnRepo = FakeTurnRepository()
            turnRepo.setTurns(listOf(testTurnWithDice(3, 4)))
            val viewModel = GameSummaryViewModel(
                gameId = 1L,
                gameRepository = gameRepo,
                getGameStatisticsUseCase = GetGameStatisticsUseCase(gameRepo, turnRepo)
            )
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.statistics)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `GameSummaryViewModel init, game not found, error state set`() =
        runTest(testDispatcher) {
            val viewModel = GameSummaryViewModel(
                gameId = 999L,
                gameRepository = FakeGameRepository(),
                getGameStatisticsUseCase = GetGameStatisticsUseCase(FakeGameRepository(), FakeTurnRepository())
            )
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)
        }
}
