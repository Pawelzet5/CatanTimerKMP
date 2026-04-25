package io.github.pawelzielinski.catantimer.catan_companion_feature.presentation.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pawelzielinski.catantimer.catan_companion_feature.domain.dataclass.DiceDistribution
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing

private const val MAX_BAR_HEIGHT_DP = 80
private const val ANIMATION_DURATION_MS = 600
private const val COUNT_LABEL_SPACE_DP = 20

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
            val targetFraction = count.toFloat() / maxCount.toFloat()

            val animatable = remember { Animatable(0f) }
            LaunchedEffect(targetFraction) {
                animatable.animateTo(
                    targetValue = targetFraction,
                    animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
                )
            }
            val animatedFraction = animatable.value

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Fixed-height container: top COUNT_LABEL_SPACE_DP reserved for count label,
                // bottom MAX_BAR_HEIGHT_DP for the bar. The count label tracks the bar top
                // via an offset so it always floats 4dp above the actual bar.
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((MAX_BAR_HEIGHT_DP + COUNT_LABEL_SPACE_DP).dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height((animatedFraction * MAX_BAR_HEIGHT_DP).dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                            ),
                    )
                    if (count > 0) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-(animatedFraction * MAX_BAR_HEIGHT_DP + 4)).dp),
                        )
                    }
                }
                Text(
                    text = sum.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = CatanSpacing.xs),
                )
            }
        }
    }
}