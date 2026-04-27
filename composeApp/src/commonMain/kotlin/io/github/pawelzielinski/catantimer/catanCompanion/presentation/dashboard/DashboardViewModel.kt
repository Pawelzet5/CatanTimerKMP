package io.github.pawelzielinski.catantimer.catanCompanion.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.PlayerRepository

class DashboardViewModel(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val _events = Channel<DashboardEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observeResumableGame()
        observeTotalGames()
        observeTotalPlayers()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.NewGameClick -> _events.trySend(DashboardEvent.NavigateToNewGame)
            is DashboardAction.ResumeGameClick -> _events.trySend(DashboardEvent.NavigateToResumeGame(action.gameId))
            DashboardAction.GamesListClick -> _events.trySend(DashboardEvent.NavigateToGamesList)
            DashboardAction.PlayersListClick -> _events.trySend(DashboardEvent.NavigateToPlayersList)
        }
    }

    private fun observeResumableGame() {
        gameRepository.getMostRecentInProgressGame()
            .onEach { game ->
                _uiState.update { it.copy(resumableGame = game, isLoading = false) }
            }
            .catch { _uiState.update { it.copy(isLoading = false) } }
            .launchIn(viewModelScope)
    }

    private fun observeTotalGames() {
        gameRepository.getAllGames()
            .onEach { games -> _uiState.update { it.copy(totalGames = games.size) } }
            .launchIn(viewModelScope)
    }

    private fun observeTotalPlayers() {
        playerRepository.getAllPlayers()
            .onEach { players -> _uiState.update { it.copy(totalPlayers = players.size) } }
            .launchIn(viewModelScope)
    }
}
