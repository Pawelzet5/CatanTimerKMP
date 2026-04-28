package io.github.pawelzielinski.catantimer.catanCompanion.presentation.components.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.DiceType

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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
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
