package org.example.project.catan_companion_feature.presentation.gameplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.catan_companion_feature.domain.dataclass.DiceDistribution
import org.example.project.catan_companion_feature.domain.dataclass.DiceRoll
import org.example.project.catan_companion_feature.domain.dataclass.toBarbarianState
import org.example.project.catan_companion_feature.domain.enums.GameExpansion
import org.example.project.catan_companion_feature.domain.repository.GameRepository
import org.example.project.catan_companion_feature.domain.repository.TurnRepository
import org.example.project.catan_companion_feature.domain.session.GameSessionCoordinator
import kotlin.time.Clock

class GameplayViewModel(
    private val gameId: Long,
    private val sessionCoordinator: GameSessionCoordinator,
    private val turnRepository: TurnRepository,
    private val gameRepository: GameRepository,
    private val timerManager: TimerManager
) : ViewModel() {
    private val _navigator = MutableStateFlow<TurnNavigator?>(null)
    private var primaryElapsedMillis: Long = 0L

    private val _uiState = MutableStateFlow(GameplayState(isLoading = true))
    val uiState: StateFlow<GameplayState> = _uiState.asStateFlow()

    private val _events = Channel<GameplayEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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

    fun onAction(action: GameplayAction) {
        when (action) {
            GameplayAction.MenuClick -> _uiState.update { it.copy(showSettingsSheet = true) }
            GameplayAction.PreviousClick -> onNavigateToPreviousTurn()
            GameplayAction.NextClick -> onNavigateToNextTurn()
            GameplayAction.JumpToCurrentClick -> onJumpToCurrentTurn()
            is GameplayAction.DiceSelected -> _uiState.update { it.copy(pendingDiceEdit = DiceRoll(action.red, action.yellow, action.event)) }
            GameplayAction.ContinueFromDiceClick -> onContinueFromDice()
            GameplayAction.ContinueFromEventClick -> onContinueFromEvent()
            GameplayAction.TimerToggleClick -> if (_uiState.value.timerState.isRunning) timerManager.stop() else timerManager.start(_uiState.value.timerState.remainingMillis)
            GameplayAction.AddTimeClick -> timerManager.addTime(10_000L)
            GameplayAction.ResetTimerClick -> timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
            GameplayAction.NextTurnClick -> onNextTurn()
            GameplayAction.InBetweenTurnClick -> onStartInBetweenTurn()
            GameplayAction.SaveHistoricalEditClick -> _uiState.update { it.copy(showHistoricalEditConfirm = true) }
            GameplayAction.DismissSettingsSheetClick -> _uiState.update { it.copy(showSettingsSheet = false) }
            GameplayAction.ViewStatisticsClick -> _uiState.update { it.copy(showSettingsSheet = false, showStatisticsPopup = true) }
            is GameplayAction.SaveSettingsClick -> onUpdateGameSettings(action.expansions, action.specialTurnRuleEnabled)
            GameplayAction.StartNewGameClick -> {
                _uiState.update { it.copy(showSettingsSheet = false) }
                _events.trySend(GameplayEvent.NavigateToGameConfig)
            }
            GameplayAction.EndGameClick -> _uiState.update { it.copy(showSettingsSheet = false, showEndGameConfirm = true) }
            GameplayAction.DismissStatisticsPopupClick -> _uiState.update { it.copy(showStatisticsPopup = false) }
            GameplayAction.ConfirmEndGameClick -> {
                _uiState.update { it.copy(showEndGameConfirm = false) }
                _events.trySend(GameplayEvent.NavigateToWinnerSelection(gameId))
            }
            GameplayAction.DismissEndGameConfirmClick -> _uiState.update { it.copy(showEndGameConfirm = false) }
            GameplayAction.ConfirmHistoricalEditClick -> onConfirmHistoricalDiceEdit()
            GameplayAction.DismissHistoricalEditConfirmClick -> _uiState.update { it.copy(showHistoricalEditConfirm = false) }
        }
    }

    private fun onContinueFromDice() {
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

    private fun onContinueFromEvent() {
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
        _uiState.update { it.copy(phase = GameplayPhase.MAIN_TIMER) }
    }

    private fun onStartInBetweenTurn() {
        primaryElapsedMillis = timerManager.stop()
        timerManager.reset(_uiState.value.game?.turnDurationMillis ?: 120_000L)
        _uiState.update { it.copy(phase = GameplayPhase.IN_BETWEEN_TIMER) }
    }

    private fun onNextTurn() {
        viewModelScope.launch {
            val elapsed = primaryElapsedMillis + timerManager.stop()
            primaryElapsedMillis = 0L
            sessionCoordinator.completeTurn(elapsed)
            _uiState.update { it.copy(phase = GameplayPhase.DICE_SELECTION, pendingDiceEdit = null) }
        }
    }

    private fun onNavigateToPreviousTurn() {
        val nav = _navigator.value?.selectPrevious() ?: return
        _navigator.value = nav
        val pendingDice = nav.selectedTurn?.let { t ->
            if (!nav.isViewingLatest && t.redDice != null && t.yellowDice != null) {
                DiceRoll(t.redDice, t.yellowDice, t.eventDice)
            } else null
        }
        _uiState.update {
            it.copy(
                displayedTurn = nav.selectedTurn,
                isViewingLatest = nav.isViewingLatest,
                pendingDiceEdit = pendingDice
            )
        }
    }

    private fun onNavigateToNextTurn() {
        val nav = _navigator.value?.selectNext() ?: return
        _navigator.value = nav
        val pendingDice = nav.selectedTurn?.let { t ->
            if (!nav.isViewingLatest && t.redDice != null && t.yellowDice != null) {
                DiceRoll(t.redDice, t.yellowDice, t.eventDice)
            } else null
        }
        _uiState.update {
            it.copy(
                displayedTurn = nav.selectedTurn,
                isViewingLatest = nav.isViewingLatest,
                pendingDiceEdit = pendingDice
            )
        }
    }

    private fun onJumpToCurrentTurn() {
        val nav = _navigator.value?.selectLatest() ?: return
        _navigator.value = nav
        _uiState.update {
            it.copy(
                displayedTurn = nav.selectedTurn,
                isViewingLatest = true,
                pendingDiceEdit = null
            )
        }
    }

    private fun onConfirmHistoricalDiceEdit() {
        val turn = _uiState.value.displayedTurn ?: return
        val dice = _uiState.value.pendingDiceEdit ?: return
        viewModelScope.launch {
            sessionCoordinator.updateTurnDice(turn, dice.red, dice.yellow, dice.event)
            _uiState.update { it.copy(showHistoricalEditConfirm = false) }
        }
    }

    private fun onUpdateGameSettings(expansions: Set<GameExpansion>, specialTurnRuleEnabled: Boolean) {
        viewModelScope.launch {
            gameRepository.updateGameSettings(gameId, expansions, specialTurnRuleEnabled)
            _uiState.update { it.copy(showSettingsSheet = false) }
        }
    }

    fun onFinishSession(winnerId: Long?) {
        viewModelScope.launch {
            sessionCoordinator.finishSession(
                finishedAt = Clock.System.now().epochSeconds,
                winnerId = winnerId
            )
        }
    }
}
