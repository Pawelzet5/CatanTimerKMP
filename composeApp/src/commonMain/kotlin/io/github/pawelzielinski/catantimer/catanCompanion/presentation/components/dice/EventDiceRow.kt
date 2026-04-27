package io.github.pawelzielinski.catantimer.catanCompanion.presentation.components.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.pawelzielinski.catantimer.catanCompanion.domain.enums.EventDiceType

@Composable
fun EventDiceRow(
    selectedType: EventDiceType?,
    onTypeSelected: (EventDiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventDiceType.entries.forEach { type ->
            EventDice(
                type = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) },
            )
        }
    }
}
