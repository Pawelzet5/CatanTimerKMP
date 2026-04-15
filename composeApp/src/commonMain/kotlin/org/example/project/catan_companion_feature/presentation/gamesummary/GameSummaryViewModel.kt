package org.example.project.catan_companion_feature.presentation.gamesummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.usecase.GetGameStatisticsUseCase
import org.example.project.core.domain.Result

class GameSummaryViewModel(
    private val gameId: Long,
    private val gameRepository: GameRepository,
    private val getGameStatisticsUseCase: GetGameStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSummaryUiState(isLoading = true))
    val uiState: StateFlow<GameSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val statsResult = getGameStatisticsUseCase(gameId)
            gameRepository.getGameById(gameId).firstOrNull()?.let { game ->
                _uiState.update { it.copy(game = game) }
            }
            when (statsResult) {
                is Result.Success -> _uiState.update {
                    it.copy(statistics = statsResult.data, isLoading = false)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(error = "Failed to load statistics", isLoading = false)
                }
            }
        }
    }
}
