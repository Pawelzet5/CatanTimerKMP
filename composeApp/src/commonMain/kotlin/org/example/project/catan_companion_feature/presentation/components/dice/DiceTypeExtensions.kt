package org.example.project.catan_companion_feature.presentation.components.dice

import androidx.compose.ui.graphics.Color
import org.example.project.catan_companion_feature.domain.enums.DiceType
import org.example.project.core.designsystem.CatanDiceRedBackground
import org.example.project.core.designsystem.CatanDiceRedDot
import org.example.project.core.designsystem.CatanDiceYellowBackground
import org.example.project.core.designsystem.CatanDiceYellowDot

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
