package io.github.pawelzielinski.catantimer.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import catantimer.composeapp.generated.resources.Res
import catantimer.composeapp.generated.resources.ic_close
import kotlinx.coroutines.launch
import io.github.pawelzielinski.catantimer.core.designsystem.CatanSpacing
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun SwipeToDeleteItem(
    deleteActionLabel: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (accessibilityModifier: Modifier) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var itemWidth by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .onSizeChanged { itemWidth = it.width.toFloat() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = deleteActionLabel,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(end = CatanSpacing.md)
            )
        }

        val accessibilityModifier = Modifier.semantics {
            customActions = listOf(
                CustomAccessibilityAction(deleteActionLabel) {
                    onDelete()
                    true
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(-itemWidth, 0f))
                        }
                    },
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            val dismissedByDistance = -offsetX.value >= itemWidth * 0.85f
                            val dismissedByFling = velocity < -1500f
                            if (dismissedByDistance || dismissedByFling) {
                                offsetX.animateTo(-itemWidth, tween(durationMillis = 200))
                                onDelete()
                                offsetX.snapTo(0f)
                            } else {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                        }
                    }
                )
        ) {
            content(accessibilityModifier)
        }
    }
}
