package io.github.pawelzielinski.catantimer.catanCompanion.presentation.playerslist

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
import io.github.pawelzielinski.catantimer.catanCompanion.domain.repository.PlayerRepository

class PlayersListViewModel(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayersListState())
    val uiState: StateFlow<PlayersListState> = _uiState.asStateFlow()

    private val _events = Channel<PlayersListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        playerRepository.getVisiblePlayers()
            .onEach { players -> _uiState.update { it.copy(players = players) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: PlayersListAction) {
        when (action) {
            PlayersListAction.BackClick -> _events.trySend(PlayersListEvent.NavigateBack)
            is PlayersListAction.PlayerClick -> _events.trySend(PlayersListEvent.NavigateToPlayerDetails(action.playerId))
            is PlayersListAction.DoneWithSelection -> _events.trySend(PlayersListEvent.PlayersSelected(action.players))
            is PlayersListAction.CreatePlayer -> viewModelScope.launch {
                playerRepository.createPlayer(action.name)
            }
            is PlayersListAction.HidePlayer -> viewModelScope.launch {
                playerRepository.hidePlayer(action.player.id)
            }
            is PlayersListAction.DeletePlayer -> viewModelScope.launch {
                if (playerRepository.canDeletePlayer(action.player.id)) {
                    playerRepository.deletePlayer(action.player.id)
                }
            }
        }
    }
}
