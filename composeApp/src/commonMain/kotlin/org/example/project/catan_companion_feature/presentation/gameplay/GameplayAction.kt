package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.gameplay

import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.EventDiceType
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.enums.GameExpansion

sealed interface GameplayAction {
    // Screen
    data object MenuClick : GameplayAction
    data object PreviousClick : GameplayAction
    data object NextClick : GameplayAction
    data object JumpToCurrentClick : GameplayAction
    data class DiceSelected(val red: Int, val yellow: Int, val event: EventDiceType?) : GameplayAction
    data object ContinueFromDiceClick : GameplayAction
    data object ContinueFromEventClick : GameplayAction
    data object TimerToggleClick : GameplayAction
    data object AddTimeClick : GameplayAction
    data object ResetTimerClick : GameplayAction
    data object NextTurnClick : GameplayAction
    data object InBetweenTurnClick : GameplayAction
    data object SaveHistoricalEditClick : GameplayAction

    // Settings sheet
    data object DismissSettingsSheetClick : GameplayAction
    data object ViewStatisticsClick : GameplayAction
    data class SaveSettingsClick(val expansions: Set<GameExpansion>, val specialTurnRuleEnabled: Boolean) : GameplayAction
    data object StartNewGameClick : GameplayAction
    data object EndGameClick : GameplayAction

    // Statistics popup
    data object DismissStatisticsPopupClick : GameplayAction

    // End game confirmation dialog
    data object ConfirmEndGameClick : GameplayAction
    data object DismissEndGameConfirmClick : GameplayAction

    // Historical edit confirmation dialog
    data object ConfirmHistoricalEditClick : GameplayAction
    data object DismissHistoricalEditConfirmClick : GameplayAction

    // Winner selection
    data class ConfirmWinnerClick(val winnerId: Long?) : GameplayAction
}
