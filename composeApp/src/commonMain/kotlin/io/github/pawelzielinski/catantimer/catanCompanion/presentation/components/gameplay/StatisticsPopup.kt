package io.github.pawelzielinski.catantimer.catanCompanion.presentation.components.gameplay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.common_cancel
import catantimer.composeapp.generated.resources.stats_title
import io.github.pawelzielinski.catantimer.catanCompanion.domain.dataclass.DiceDistribution
import io.github.pawelzielinski.catantimer.catanCompanion.presentation.components.charts.DiceStatisticsChart
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import io.github.pawelzielinski.catantimer.core.designsystem.components.CatanButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatisticsPopup(
    distribution: DiceDistribution,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CatanSpacing.md),
            ) {
                Text(
                    text = stringResource(Res.string.stats_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = CatanSpacing.md),
                )
                DiceStatisticsChart(
                    distribution = distribution,
                    modifier = Modifier.fillMaxWidth(),
                )
                CatanButton(
                    text = stringResource(Res.string.common_cancel),
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = CatanSpacing.md),
                )
            }
        }
    }
}
