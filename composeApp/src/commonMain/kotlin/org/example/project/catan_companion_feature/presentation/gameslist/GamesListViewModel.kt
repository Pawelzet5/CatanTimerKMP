package org.example.project.catan_companion_feature.presentation.gameslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.enums.GameStatus
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.presentation.service.HapticService

class GamesListViewModel(
    private val gameRepository: GameRepository,
    private val hapticService: HapticService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesListState())
    val uiState: StateFlow<GamesListState> = _uiState.asStateFlow()

    private val _events = Channel<GamesListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        combine(
            gameRepository.getInProgressGames(),
            gameRepository.getCompletedGames()
        ) { inProgress, completed ->
            GamesListState(inProgressGames = inProgress, completedGames = completed)
        }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun onAction(action: GamesListAction) {
        when (action) {
            GamesListAction.BackClick -> _events.trySend(GamesListEvent.NavigateBack)
            is GamesListAction.GameClick -> {
                val game = _uiState.value.inProgressGames.find { it.id == action.gameId }
                    ?: _uiState.value.completedGames.find { it.id == action.gameId }
                val event = if (game?.status == GameStatus.IN_PROGRESS)
                    GamesListEvent.NavigateToGameplay(action.gameId)
                else
                    GamesListEvent.NavigateToGameSummary(action.gameId)
                _events.trySend(event)
            }
            is GamesListAction.RequestDeleteGame -> _uiState.value = _uiState.value.copy(gameToDelete = action.game)
            GamesListAction.ConfirmDeleteGame -> {
                val gameId = _uiState.value.gameToDelete?.id ?: return
                _uiState.value = _uiState.value.copy(gameToDelete = null)
                hapticService.vibrateOnce()
                viewModelScope.launch { gameRepository.deleteGame(gameId) }
            }
            GamesListAction.DismissDeleteGame -> _uiState.value = _uiState.value.copy(gameToDelete = null)
        }
    }
}
