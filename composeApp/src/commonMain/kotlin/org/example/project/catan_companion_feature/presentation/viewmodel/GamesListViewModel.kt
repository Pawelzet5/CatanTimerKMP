package org.example.project.catan_companion_feature.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.presentation.state.GamesListUiState

class GamesListViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamesListUiState())
    val uiState: StateFlow<GamesListUiState> = _uiState.asStateFlow()

    init {
        combine(
            gameRepository.getInProgressGames(),
            gameRepository.getCompletedGames()
        ) { inProgress, completed ->
            GamesListUiState(inProgressGames = inProgress, completedGames = completed)
        }
            .onEach { state -> _uiState.value = state }
            .launchIn(viewModelScope)
    }

    fun onDeleteGame(game: Game) {
        viewModelScope.launch { gameRepository.deleteGame(game.id) }
    }
}
