package org.example.project.catan_companion_feature.presentation.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.dataclass.DiceDistribution
import org.example.project.catan_companion_feature.domain.dataclass.DiceRoll
import org.example.project.catan_companion_feature.domain.dataclass.toBarbarianState
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.example.project.catan_companion_feature.domain.session.GameSessionCoordinator

class GameplayViewModel(
    private val gameId: Long,
    private val sessionCoordinator: GameSessionCoordinator,
    private val turnRepository: TurnRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val timerManager = TimerManager(viewModelScope)
    private val _navigator = MutableStateFlow<TurnNavigator?>(null)

    private val _uiState = MutableStateFlow(GameplayUiState(isLoading = true))
    val uiState: StateFlow<GameplayUiState> = _uiState.asStateFlow()

    init {
        startSession()
        observeSession()
        observeTimer()
        observeTurnsForLiveStats()
    }

    private fun startSession() {
        viewModelScope.launch {
            sessionCoordinator.startSession(gameId)
        }
    }

    private fun observeSession() {
        sessionCoordinator.currentSession
            .filterNotNull()
            .onEach { session ->
                val navigator = TurnNavigator.from(session)
                _navigator.value = navigator
                _uiState.update {
                    it.copy(
                        game = session.game,
                        currentTurn = session.latestTurn,
                        displayedTurn = navigator.selectedTurn,
                        isViewingLatest = navigator.isViewingLatest,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTimer() {
        timerManager.state
            .onEach { timerState ->
                _uiState.update { it.copy(timerState = timerState) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTurnsForLiveStats() {
        turnRepository.getTurnsForGame(gameId)
            .onEach { turns ->
                _uiState.update {
                    it.copy(
                        barbarianState = turns.toBarbarianState(),
                        diceDistribution = DiceDistribution(
                            turns.mapNotNull { t -> t.diceSum }
                                .groupingBy { it }.eachCount()
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onDiceSelected(red: Int, yellow: Int, event: EventDiceType?) {
        _uiState.update { it.copy(pendingDiceEdit = DiceRoll(red, yellow, event)) }
    }

    fun onContinueFromDice() {
        val dice = _uiState.value.pendingDiceEdit ?: return
        viewModelScope.launch {
            sessionCoordinator.updateSelectedTurnDice(dice.red, dice.yellow, dice.event)
            val phase = if (dice.sum == 7) GameplayPhase.EVENT else GameplayPhase.MAIN_TIMER
            if (phase == GameplayPhase.MAIN_TIMER) {
                timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
            }
            _uiState.update { it.copy(phase = phase, pendingDiceEdit = null) }
        }
    }

    fun onContinueFromEvent() {
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
        _uiState.update { it.copy(phase = GameplayPhase.MAIN_TIMER) }
    }

    fun onStartTimer() {
        timerManager.start(_uiState.value.timerState.remainingMillis)
    }

    fun onStopTimer() {
        timerManager.stop()
    }

    fun onAddTime() {
        timerManager.addTime(10_000L)
    }

    fun onResetTimer() {
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
    }

    fun onStartInBetweenTurn() {
        timerManager.stop()
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
        _uiState.update { it.copy(phase = GameplayPhase.IN_BETWEEN_TIMER) }
    }

    fun onNextTurn() {
        viewModelScope.launch {
            val elapsed = timerManager.stop()
            sessionCoordinator.completeTurn(elapsed)
            _uiState.update { it.copy(phase = GameplayPhase.DICE_SELECTION, pendingDiceEdit = null) }
        }
    }

    fun onEndGame() {
        // Navigation to WinnerSelection is handled by the screen via a SharedFlow or callback
        // finishSession will be called after winner selection
    }

    fun onNavigateToPreviousTurn() {
        val nav = _navigator.value?.selectPrevious() ?: return
        _navigator.value = nav
        _uiState.update { it.copy(displayedTurn = nav.selectedTurn, isViewingLatest = nav.isViewingLatest) }
    }

    fun onNavigateToNextTurn() {
        val nav = _navigator.value?.selectNext() ?: return
        _navigator.value = nav
        _uiState.update { it.copy(displayedTurn = nav.selectedTurn, isViewingLatest = nav.isViewingLatest) }
    }

    fun onJumpToCurrentTurn() {
        val nav = _navigator.value?.selectLatest() ?: return
        _navigator.value = nav
        _uiState.update { it.copy(displayedTurn = nav.selectedTurn, isViewingLatest = true) }
    }

    fun onSaveEdit() {
        val dice = _uiState.value.pendingDiceEdit ?: return
        viewModelScope.launch {
            sessionCoordinator.updateSelectedTurnDice(dice.red, dice.yellow, dice.event)
            _uiState.update { it.copy(isEditing = false, pendingDiceEdit = null) }
        }
    }

    fun onCancelEdit() {
        _uiState.update { it.copy(isEditing = false, pendingDiceEdit = null) }
    }

    fun onUpdateGameSettings(expansions: Set<GameExpansion>, specialTurnRuleEnabled: Boolean) {
        viewModelScope.launch {
            gameRepository.updateGameSettings(gameId, expansions, specialTurnRuleEnabled)
            _uiState.update { it.copy(showSettingsSheet = false) }
        }
    }

    fun onFinishSession(winnerId: Long?) {
        viewModelScope.launch {
            sessionCoordinator.finishSession(
                finishedAt = System.currentTimeMillis(),
                winnerId = winnerId
            )
        }
    }

    fun onShowStatisticsPopup() {
        _uiState.update { it.copy(showStatisticsPopup = true) }
    }

    fun onHideStatisticsPopup() {
        _uiState.update { it.copy(showStatisticsPopup = false) }
    }

    fun onShowSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = true) }
    }

    fun onHideSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = false) }
    }
}
