package org.example.project.catan_companion_feature.presentation.components.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.catan_companion_feature.domain.enums.DiceType
import org.example.project.core.designsystem.CatanSpacing

@Composable
fun DiceRow(
    selectedValue: Int?,
    diceType: DiceType,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val background = diceType.backgroundColor()
    val dotColor = diceType.dotColor()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..6).forEach { value ->
            Dice(
                value = value,
                isSelected = selectedValue == value,
                backgroundColor = background,
                dotColor = dotColor,
                onClick = { onValueSelected(value) },
            )
        }
    }
}
