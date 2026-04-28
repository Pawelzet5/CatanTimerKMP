package io.github.pawelzielinski.catantimer.catanCompanion.presentation.components.dice

import androidx.compose.ui.graphics.Color
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.DiceType
import io.github.pawelzielinski.catantimer.core.designsystem.CatanDiceRedBackground
import io.github.pawelzielinski.catantimer.core.designsystem.CatanDiceRedDot
import io.github.pawelzielinski.catantimer.core.designsystem.CatanDiceYellowBackground
import io.github.pawelzielinski.catantimer.core.designsystem.CatanDiceYellowDot

fun DiceType.backgroundColor(): Color = when (this) {
    DiceType.RedDice -> CatanDiceRedBackground
    DiceType.YellowDice -> CatanDiceYellowBackground
    DiceType.EventDice -> Color.White
}

fun DiceType.dotColor(): Color = when (this) {
    DiceType.RedDice -> CatanDiceRedDot
    DiceType.YellowDice -> CatanDiceYellowDot
    DiceType.EventDice -> Color.Black
}
