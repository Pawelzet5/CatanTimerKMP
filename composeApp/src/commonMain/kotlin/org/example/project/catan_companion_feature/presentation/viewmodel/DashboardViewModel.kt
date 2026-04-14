package org.example.project.catan_companion_feature.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.presentation.state.DashboardUiState

class DashboardViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeResumableGame()
    }

    private fun observeResumableGame() {
        gameRepository.getMostRecentInProgressGame()
            .onEach { game ->
                _uiState.update { it.copy(resumableGame = game, isLoading = false) }
            }
            .catch { _uiState.update { it.copy(isLoading = false) } }
            .launchIn(viewModelScope)
    }
}
