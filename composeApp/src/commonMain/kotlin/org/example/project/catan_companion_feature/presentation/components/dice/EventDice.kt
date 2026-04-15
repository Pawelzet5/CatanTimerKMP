package org.example.project.catan_companion_feature.presentation.components.dice

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.core.designsystem.CatanDiceEventBackground
import org.example.project.core.designsystem.CatanDiceSelectedBorder

private fun EventDiceType.symbol(): String = when (this) {
    EventDiceType.POLITICS -> "⚔"
    EventDiceType.SCIENCE -> "🔬"
    EventDiceType.TRADE -> "💰"
    EventDiceType.BARBARIANS -> "🚢"
}

@Composable
fun EventDice(
    type: EventDiceType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isSelected) CatanDiceSelectedBorder else Color.Transparent
    val bgColor = CatanDiceEventBackground

    Box(
        modifier = modifier
            .size(50.dp)
            .clip(shape)
            .drawBehind { drawRect(bgColor) }
            .border(width = 3.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = type.symbol(),
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
