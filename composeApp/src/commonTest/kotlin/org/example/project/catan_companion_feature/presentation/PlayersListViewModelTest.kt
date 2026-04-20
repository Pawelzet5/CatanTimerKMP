package org.example.project.catan_companion_feature.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.example.project.catan_companion_feature.data.fakes.repository.FakePlayerRepository
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListAction
import org.example.project.catan_companion_feature.presentation.playerslist.PlayersListViewModel
import org.example.project.catan_companion_feature.testPlayer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayersListViewModelTest {

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
    fun `PlayersListViewModel init, players in repository, all players loaded into state`() =
        runTest(testDispatcher) {
            val repo = FakePlayerRepository()
            repo.setPlayers(listOf(testPlayer(1L, "Alice"), testPlayer(2L, "Bob")))
            val viewModel = PlayersListViewModel(repo)
            assertEquals(2, viewModel.uiState.value.players.size)
        }

    @Test
    fun `PlayersListViewModel createPlayer, name provided, player added to repository`() =
        runTest(testDispatcher) {
            val repo = FakePlayerRepository()
            val viewModel = PlayersListViewModel(repo)
            viewModel.onAction(PlayersListAction.CreatePlayer("Charlie"))
            advanceUntilIdle()
            assertTrue(repo.createdPlayers.contains("Charlie"))
        }
}
