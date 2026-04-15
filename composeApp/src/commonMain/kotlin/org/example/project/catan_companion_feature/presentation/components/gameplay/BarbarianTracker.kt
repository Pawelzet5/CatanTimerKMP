package org.example.project.catan_companion_feature.presentation.components.gameplay

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.catan_companion_feature.domain.dataclass.BarbarianState
import org.example.project.core.designsystem.CatanSpacing

@Composable
fun BarbarianTracker(
    state: BarbarianState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(CatanSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🚢 ${state.position}/8",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
