package org.example.project.catan_companion_feature.presentation.components.timer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.timer_warning_low
import org.example.project.core.designsystem.CatanSuccess
import org.example.project.core.designsystem.CatanWarning
import org.jetbrains.compose.resources.stringResource

private const val WARNING_THRESHOLD_MILLIS = 30_000L
private const val DANGER_THRESHOLD_MILLIS = 10_000L

private fun Long.toTimerDisplay(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

@Composable
fun GameTimer(
    remainingMillis: Long,
    totalMillis: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (totalMillis > 0L) {
        (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    val isWarning = remainingMillis in (DANGER_THRESHOLD_MILLIS + 1)..WARNING_THRESHOLD_MILLIS
    val isDanger = remainingMillis in 1..DANGER_THRESHOLD_MILLIS

    val timerColor = when {
        isDanger -> MaterialTheme.colorScheme.error
        isWarning -> CatanWarning
        else -> MaterialTheme.colorScheme.onSurface
    }
    val progressColor = when {
        isDanger -> MaterialTheme.colorScheme.error
        isWarning -> CatanWarning
        else -> CatanSuccess
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = remainingMillis.toTimerDisplay(),
            fontSize = 72.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = timerColor,
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
        )
        if (isWarning || isDanger) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "⚠ ${stringResource(Res.string.timer_warning_low)}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDanger) MaterialTheme.colorScheme.error else CatanWarning,
            )
        }
    }
}
