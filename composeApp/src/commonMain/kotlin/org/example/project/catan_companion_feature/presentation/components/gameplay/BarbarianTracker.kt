package org.example.project.catan_companion_feature.presentation.components.gameplay

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.ic_barbarians
import org.example.project.catan_companion_feature.domain.dataclass.BarbarianState
import org.example.project.core.designsystem.CatanSpacing
import org.example.project.core.designsystem.catanColors
import org.jetbrains.compose.resources.painterResource

@Composable
fun BarbarianTracker(
    state: BarbarianState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.catanColors.borderAccent, shape)
            .padding(horizontal = CatanSpacing.sm, vertical = CatanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.xs),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_barbarians),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = "${state.position} / 7",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
