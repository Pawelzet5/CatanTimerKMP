package org.example.project.catan_companion_feature.presentation.playerslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository

class PlayersListViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayersListState())
    val uiState: StateFlow<PlayersListState> = _uiState.asStateFlow()

    init {
        playerRepository.getVisiblePlayers()
            .onEach { players -> _uiState.update { it.copy(players = players) } }
            .launchIn(viewModelScope)
    }

    fun onCreatePlayer(name: String) {
        viewModelScope.launch { playerRepository.createPlayer(name) }
    }

    fun onHidePlayer(player: Player) {
        viewModelScope.launch { playerRepository.hidePlayer(player.id) }
    }

    fun onDeletePlayer(player: Player) {
        viewModelScope.launch {
            if (playerRepository.canDeletePlayer(player.id)) {
                playerRepository.deletePlayer(player.id)
            }
        }
    }
}
