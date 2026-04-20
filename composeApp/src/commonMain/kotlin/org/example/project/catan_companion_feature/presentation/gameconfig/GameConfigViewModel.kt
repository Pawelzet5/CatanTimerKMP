package org.example.project.catan_companion_feature.presentation.gameconfig

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
import org.example.project.catan_companion_feature.domain.usecase.CreateGameUseCase
import org.example.project.core.domain.Result
import org.example.project.core.presentation.toUiText

class GameConfigViewModel(
    private val playerRepository: PlayerRepository,
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameConfigState())
    val uiState: StateFlow<GameConfigState> = _uiState.asStateFlow()

    private val _events = Channel<GameConfigEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadAvailablePlayers()
    }

    fun onAction(action: GameConfigAction) {
        when (action) {
            GameConfigAction.BackClick -> _events.trySend(GameConfigEvent.NavigateBack)
            is GameConfigAction.PlayerToggled -> onPlayerToggled(action)
            is GameConfigAction.ExpansionToggled -> onExpansionToggled(action)
            GameConfigAction.SpecialTurnRuleToggled -> {
                _uiState.update { it.copy(specialTurnRuleEnabled = !it.specialTurnRuleEnabled) }
                revalidate()
            }
            is GameConfigAction.TurnDurationChanged -> {
                _uiState.update { it.copy(turnDurationMillis = action.millis) }
                revalidate()
            }
            is GameConfigAction.PlayersSelected -> {
                val toAdd = action.players.filter { it !in _uiState.value.selectedPlayers }
                if (toAdd.isNotEmpty()) {
                    _uiState.update { it.copy(selectedPlayers = it.selectedPlayers + toAdd) }
                    revalidate()
                }
            }
            is GameConfigAction.PlayerCountSelected -> {
                _uiState.update {
                    it.copy(
                        numberOfPlayers = action.count,
                        selectedPlayers = it.selectedPlayers.take(action.count)
                    )
                }
                revalidate()
            }
            GameConfigAction.StartGameClick -> startGame()
            GameConfigAction.AddPlayerClick -> _events.trySend(GameConfigEvent.NavigateToPlayerSelection)
        }
    }

    private fun loadAvailablePlayers() {
        playerRepository.getVisiblePlayers()
            .onEach { players ->
                _uiState.update { it.copy(availablePlayers = players) }
            }
            .launchIn(viewModelScope)
    }

    private fun onPlayerToggled(action: GameConfigAction.PlayerToggled) {
        val current = _uiState.value.selectedPlayers
        val updated = if (current.contains(action.player)) current - action.player else current + action.player
        _uiState.update { it.copy(selectedPlayers = updated) }
        revalidate()
    }

    private fun onExpansionToggled(action: GameConfigAction.ExpansionToggled) {
        val current = _uiState.value.expansions
        val updated = if (current.contains(action.expansion)) current - action.expansion else current + action.expansion
        _uiState.update { it.copy(expansions = updated) }
        revalidate()
    }

    private fun startGame() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = createGameUseCase(
                turnDurationMillis = state.turnDurationMillis,
                expansions = state.expansions,
                specialTurnRuleEnabled = state.specialTurnRuleEnabled,
                playerIds = state.selectedPlayers.map { it.id }
            )
            when (result) {
                is Result.Success -> _events.trySend(GameConfigEvent.NavigateToGameplay(result.data))
                is Result.Failure -> _uiState.update {
                    it.copy(validationError = result.error.toUiText())
                }
            }
        }
    }

    private fun revalidate() {
        val state = _uiState.value
        val hasCorrectCount = state.selectedPlayers.size == state.numberOfPlayers
        val specialTurnRuleValid = !state.specialTurnRuleEnabled || state.numberOfPlayers >= 5
        val valid = hasCorrectCount && specialTurnRuleValid
        _uiState.update { it.copy(isValid = valid, validationError = if (valid) null else it.validationError) }
    }
}
