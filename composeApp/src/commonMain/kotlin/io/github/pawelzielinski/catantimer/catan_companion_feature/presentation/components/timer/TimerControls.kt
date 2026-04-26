package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.ic_more_time
import catantimer.composeapp.generated.resources.ic_pause
import catantimer.composeapp.generated.resources.ic_play
import catantimer.composeapp.generated.resources.ic_restart
import catantimer.composeapp.generated.resources.timer_add_10
import catantimer.composeapp.generated.resources.timer_reset
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
        horizontalArrangement = Arrangement.spacedBy(CatanSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryTimerButton(
            icon = Res.drawable.ic_restart,
            label = stringResource(Res.string.timer_reset),
            onClick = onReset,
        )
        PrimaryTimerButton(
            icon = if (isRunning) Res.drawable.ic_pause else Res.drawable.ic_play,
            onClick = onStartStop,
        )
        SecondaryTimerButton(
            icon = Res.drawable.ic_more_time,
            label = stringResource(Res.string.timer_add_10),
            onClick = onAddTime,
        )
    }
}

@Composable
private fun PrimaryTimerButton(icon: DrawableResource, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun SecondaryTimerButton(icon: DrawableResource, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
