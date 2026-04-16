package org.example.project.catan_companion_feature.presentation.gameconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.dataclass.Player
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.repository.PlayerRepository
import org.example.project.catan_companion_feature.domain.usecase.CreateGameUseCase
import org.example.project.core.domain.Result

class GameConfigViewModel(
    private val playerRepository: PlayerRepository,
    private val createGameUseCase: CreateGameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameConfigUiState())
    val uiState: StateFlow<GameConfigUiState> = _uiState.asStateFlow()

    private val _navigateToGameplay = MutableSharedFlow<Long>(replay = 0)
    val navigateToGameplay: SharedFlow<Long> = _navigateToGameplay.asSharedFlow()

    init {
        loadAvailablePlayers()
    }

    private fun loadAvailablePlayers() {
        playerRepository.getVisiblePlayers()
            .onEach { players ->
                _uiState.update { it.copy(availablePlayers = players) }
            }
            .launchIn(viewModelScope)
    }

    fun onPlayerToggled(player: Player) {
        val current = _uiState.value.selectedPlayers
        val updated = if (current.contains(player)) current - player else current + player
        _uiState.update { it.copy(selectedPlayers = updated) }
        revalidate()
    }

    fun onExpansionToggled(expansion: GameExpansion) {
        val current = _uiState.value.expansions
        val updated = if (current.contains(expansion)) current - expansion else current + expansion
        _uiState.update { it.copy(expansions = updated) }
        revalidate()
    }

    fun onSpecialTurnRuleToggled() {
        _uiState.update { it.copy(specialTurnRuleEnabled = !it.specialTurnRuleEnabled) }
        revalidate()
    }

    fun onTurnDurationChanged(millis: Long) {
        _uiState.update { it.copy(turnDurationMillis = millis) }
        revalidate()
    }

    fun onPlayersSelectedByIds(ids: List<Long>) {
        val toAdd = _uiState.value.availablePlayers.filter { it.id in ids }
        toAdd.forEach { player ->
            if (!_uiState.value.selectedPlayers.contains(player)) {
                _uiState.update { it.copy(selectedPlayers = it.selectedPlayers + player) }
            }
        }
        revalidate()
    }

    fun onPlayerCountSelected(count: Int) {
        _uiState.update { it.copy(
            numberOfPlayers = count,
            selectedPlayers = it.selectedPlayers.take(count)
        ) }
        revalidate()
    }

    fun onStartGame() {
        viewModelScope.launch {
            val state = _uiState.value
            val result = createGameUseCase(
                turnDurationMillis = state.turnDurationMillis,
                expansions = state.expansions,
                specialTurnRuleEnabled = state.specialTurnRuleEnabled,
                playerIds = state.selectedPlayers.map { it.id }
            )
            when (result) {
                is Result.Success -> _navigateToGameplay.emit(result.data)
                is Result.Failure -> _uiState.update {
                    it.copy(validationError = "Invalid game configuration")
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
