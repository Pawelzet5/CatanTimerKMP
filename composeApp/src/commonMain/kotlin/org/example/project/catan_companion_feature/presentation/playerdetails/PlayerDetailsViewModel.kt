package org.example.project.catan_companion_feature.presentation.playerdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository

class PlayerDetailsViewModel(
    private val playerId: Long,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerDetailsUiState())
    val uiState: StateFlow<PlayerDetailsUiState> = _uiState.asStateFlow()

    init {
        playerRepository.getPlayerById(playerId)
            .onEach { player -> _uiState.update { it.copy(player = player) } }
            .launchIn(viewModelScope)
    }

    fun onUpdateName(name: String) {
        viewModelScope.launch {
            _uiState.value.player?.let { playerRepository.updatePlayer(it.copy(name = name)) }
        }
    }

    fun onHidePlayer() {
        viewModelScope.launch { playerRepository.hidePlayer(playerId) }
    }

    fun onDeletePlayer() {
        viewModelScope.launch { playerRepository.deletePlayer(playerId) }
    }
}
