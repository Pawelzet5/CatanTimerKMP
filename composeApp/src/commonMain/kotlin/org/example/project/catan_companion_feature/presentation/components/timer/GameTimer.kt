package org.example.project.catan_companion_feature.presentation.components.timer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private fun Long.toTimerDisplay(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun GameTimer(
    remainingMillis: Long,
    modifier: Modifier = Modifier
) {
    Text(
        text = remainingMillis.toTimerDisplay(),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}
