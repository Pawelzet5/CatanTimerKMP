package org.example.project.catan_companion_feature.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository

class DashboardViewModel(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeResumableGame()
        observeTotalGames()
        observeTotalPlayers()
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
