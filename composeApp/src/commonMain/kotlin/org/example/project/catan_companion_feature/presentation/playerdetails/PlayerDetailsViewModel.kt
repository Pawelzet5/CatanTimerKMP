package org.example.project.catan_companion_feature.presentation.playerdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository

class PlayerDetailsViewModel(
    private val playerId: Long,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerDetailsState())
    val uiState: StateFlow<PlayerDetailsState> = _uiState.asStateFlow()

    private val _events = Channel<PlayerDetailsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        playerRepository.getPlayerById(playerId)
            .onEach { player -> _uiState.update { it.copy(player = player) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: PlayerDetailsAction) {
        when (action) {
            PlayerDetailsAction.BackClick -> _events.trySend(PlayerDetailsEvent.NavigateBack)
            is PlayerDetailsAction.UpdateName -> viewModelScope.launch {
                _uiState.value.player?.let { playerRepository.updatePlayer(it.copy(name = action.name)) }
            }
            PlayerDetailsAction.HidePlayerClick -> viewModelScope.launch {
                playerRepository.hidePlayer(playerId)
                _events.trySend(PlayerDetailsEvent.NavigateBack)
            }
            PlayerDetailsAction.DeletePlayerClick -> viewModelScope.launch {
                playerRepository.deletePlayer(playerId)
                _events.trySend(PlayerDetailsEvent.NavigateBack)
            }
        }
    }
}
