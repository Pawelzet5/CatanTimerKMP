package org.example.project.catan_companion_feature.presentation.gameplay

import org.example.project.catan_companion_feature.domain.dataclass.BarbarianState
import org.example.project.catan_companion_feature.domain.dataclass.DiceDistribution
import org.example.project.catan_companion_feature.domain.dataclass.DiceRoll
import org.example.project.catan_companion_feature.domain.dataclass.Game
import org.example.project.catan_companion_feature.domain.dataclass.Turn

data class GameplayUiState(
    val game: Game? = null,
    val currentTurn: Turn? = null,
    val displayedTurn: Turn? = null,
    val isViewingLatest: Boolean = true,
    val phase: GameplayPhase = GameplayPhase.DICE_SELECTION,
    val timerState: TimerState = TimerState(),
    val barbarianState: BarbarianState? = null,
    val diceDistribution: DiceDistribution? = null,
    val isEditing: Boolean = false,
    val pendingDiceEdit: DiceRoll? = null,
    val showStatisticsPopup: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showEndGameConfirm: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class GameplayPhase {
    DICE_SELECTION,
    EVENT,
    MAIN_TIMER,
    IN_BETWEEN_TIMER
}
