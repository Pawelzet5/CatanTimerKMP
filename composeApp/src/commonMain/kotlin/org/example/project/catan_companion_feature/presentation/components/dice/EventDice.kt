package org.example.project.catan_companion_feature.presentation.components.dice

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.ic_barbarians
import catantimer.composeapp.generated.resources.ic_politics
import catantimer.composeapp.generated.resources.ic_science
import catantimer.composeapp.generated.resources.ic_trade
import org.example.project.catan_companion_feature.domain.enums.EventDiceType
import org.example.project.core.designsystem.CatanDiceEventBackground
import org.example.project.core.designsystem.CatanDiceSelectedBorder
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private fun EventDiceType.icon(): DrawableResource = when (this) {
    EventDiceType.POLITICS -> Res.drawable.ic_politics
    EventDiceType.SCIENCE -> Res.drawable.ic_science
    EventDiceType.TRADE -> Res.drawable.ic_trade
    EventDiceType.BARBARIANS -> Res.drawable.ic_barbarians
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
        Icon(
            painter = painterResource(type.icon()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(40.dp),
        )
    }
}
