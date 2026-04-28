package io.github.pawelzielinski.catantimer.catanCompanion.presentation.gameplay

import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.BarbarianState
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.DiceDistribution
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.DiceRoll
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Game
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.Turn

data class GameplayState(
    val game: Game? = null,
    val currentTurn: Turn? = null,
    val displayedTurn: Turn? = null,
    val isViewingLatest: Boolean = true,
    val phase: GameplayPhase = GameplayPhase.DICE_SELECTION,
    val eventStep: EventStep? = null,
    val timerState: TimerState = TimerState(),
    val barbarianState: BarbarianState? = null,
    val diceDistribution: DiceDistribution? = null,
    val pendingDiceEdit: DiceRoll? = null,
    val showStatisticsPopup: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val showEndGameConfirm: Boolean = false,
    val showHistoricalEditConfirm: Boolean = false,
    val isLoading: Boolean = false
)

enum class GameplayPhase {
    DICE_SELECTION,
    EVENT,
    MAIN_TIMER,
    IN_BETWEEN_TIMER
}

enum class EventStep {
    BARBARIANS,
    ROBBER
}
