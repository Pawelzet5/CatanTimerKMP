package org.example.project.catan_companion_feature.presentation.components.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.core.designsystem.CatanSpacing

@Composable
fun EventDiceRow(
    selectedType: EventDiceType?,
    onTypeSelected: (EventDiceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.sm),
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
