package org.example.project.catan_companion_feature.presentation.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.example.project.catan_companion_feature.domain.dataclass.DiceDistribution
import org.example.project.core.designsystem.CatanSpacing

private const val MAX_BAR_HEIGHT_DP = 80
private const val ANIMATION_DURATION_MS = 400

@Composable
fun DiceStatisticsChart(
    distribution: DiceDistribution,
    modifier: Modifier = Modifier
) {
    val maxCount = distribution.counts.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        (2..12).forEach { sum ->
            val count = distribution.counts[sum] ?: 0
            val fraction = count.toFloat() / maxCount.toFloat()
            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
                label = "bar_$sum",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(MAX_BAR_HEIGHT_DP.dp)
                        .graphicsLayer {
                            scaleY = animatedFraction
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                        ),
                )
                Text(
                    text = sum.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = CatanSpacing.xs),
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
