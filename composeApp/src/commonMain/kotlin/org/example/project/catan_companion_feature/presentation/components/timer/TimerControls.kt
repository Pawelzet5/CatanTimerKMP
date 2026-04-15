package org.example.project.catan_companion_feature.presentation.components.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.timer_add_10
import catantimer.composeapp.generated.resources.timer_reset
import catantimer.composeapp.generated.resources.timer_start
import catantimer.composeapp.generated.resources.timer_stop
import org.example.project.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimerControls(
    isRunning: Boolean,
    onStartStop: () -> Unit,
    onAddTime: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onStartStop) {
            Text(
                text = if (isRunning) {
                    stringResource(Res.string.timer_stop)
                } else {
                    stringResource(Res.string.timer_start)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        TextButton(onClick = onAddTime) {
            Text(
                text = stringResource(Res.string.timer_add_10),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        TextButton(onClick = onReset) {
            Text(
                text = stringResource(Res.string.timer_reset),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
