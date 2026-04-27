package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gamesummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.GameRepository
import io.github.pawelzielinski.catantimer.catanCompanion.domain.usecase.GetGameStatisticsUseCase
import io.github.pawelzielinski.catantimer.core.domain.Result

class GameSummaryViewModel(
    private val gameId: Long,
    private val gameRepository: GameRepository,
    private val getGameStatisticsUseCase: GetGameStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSummaryState(isLoading = true))
    val uiState: StateFlow<GameSummaryState> = _uiState.asStateFlow()

    private val _events = Channel<GameSummaryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadSummary()
    }

    fun onAction(action: GameSummaryAction) {
        when (action) {
            GameSummaryAction.BackClick -> _events.trySend(GameSummaryEvent.NavigateBack)
            GameSummaryAction.HomeClick -> _events.trySend(GameSummaryEvent.NavigateHome)
        }
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
