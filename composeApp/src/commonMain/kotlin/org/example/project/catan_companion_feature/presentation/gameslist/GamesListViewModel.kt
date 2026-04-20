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
import org.example.project.catan_companion_feature.domain.repository.GameRepository

class GamesListViewModel(
    private val gameRepository: GameRepository
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
            is GamesListAction.GameClick -> _events.trySend(GamesListEvent.NavigateToGame(action.gameId))
            is GamesListAction.DeleteGameClick -> viewModelScope.launch {
                gameRepository.deleteGame(action.game.id)
            }
        }
    }
}
