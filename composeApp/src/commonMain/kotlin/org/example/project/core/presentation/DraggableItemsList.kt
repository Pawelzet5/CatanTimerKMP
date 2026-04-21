package org.example.project.core.presentation

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ─── Public API ───────────────────────────────────────────────────────────────

/**
 * A generic LazyColumn where items can be reordered by long-pressing and dragging.
 *
 * The dragged item is visually elevated (scale + shadow). Displaced neighbours animate
 * smoothly to their new positions via [Modifier.animateItem].
 *
 * [onOrderChanged] fires exactly once when the drag gesture ends successfully.
 * It is NOT called for cancelled gestures — the list reverts to its pre-drag order.
 *
 * IMPORTANT: always apply the [modifier] received in [itemContent] to the outermost
 * composable of your item. It carries the graphicsLayer transforms (translation, scale,
 * shadow). Ignoring it means the drag animation will not appear.
 */
@Composable
fun <T> DraggableItemsLazyColumn(
    items: List<T>,
    key: (T) -> Any,
    onOrderChanged: (List<T>) -> Unit,
    itemContent: @Composable (index: Int, item: T, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    onDragStart: () -> Unit = {},
) {
    val localItems = remember { mutableStateListOf<T>() }

    LaunchedEffect(items) {
        localItems.clear()
        localItems.addAll(items)
    }

    val scope = rememberCoroutineScope()
    val overscrollJob = remember { mutableStateOf<Job?>(null) }
    val dragDropState = rememberDragDropListState(
        lazyListState = lazyListState,
        onMove = { from, to -> localItems.move(from, to) }
    )

    LazyColumn(
        modifier = modifier.dragGestureHandler(
            scope = scope,
            state = dragDropState,
            overscrollJob = overscrollJob,
            onDragStart = onDragStart,
            onDragEnd = { onOrderChanged(localItems.toList()) }
        ),
        state = lazyListState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(localItems, key = key) { item ->
            val index = localItems.indexOf(item)
            val isDragged = index == dragDropState.draggedItemIndex
            val displacement = if (isDragged) dragDropState.elementDisplacement else null
            // animateItem() must be captured here while LazyItemScope is the implicit receiver.
            // It cannot be called from inside itemContent because LazyItemScope is gone by then.
            // The dragged item is excluded — its position is already controlled by graphicsLayer.
            val animatePlacementModifier = if (!isDragged) Modifier.animateItem() else Modifier

            DraggableItem(displacementOffset = displacement) { baseModifier ->
                val itemModifier = if (isDragged) {
                    baseModifier.graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                        shadowElevation = 8.dp.toPx()
                    }
                } else {
                    baseModifier.then(animatePlacementModifier)
                }
                itemContent(index, item, itemModifier)
            }
        }
    }
}

// ─── Drag-and-drop state ──────────────────────────────────────────────────────

/**
 * Tracks all mutable drag state for one active gesture. Owned by [DraggableItemsLazyColumn]
 * and never shared outside the file.
 *
 * Coordinate system: all offsets are in pixels relative to the viewport's top edge, matching
 * the values reported by [LazyListItemInfo.offset]. Positive Y is downward.
 */
private class ItemListDragAndDropState(
    private val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    /** Total Y-distance the finger has travelled since [onDragStart]. Positive = downward. */
    private var draggedDistance by mutableStateOf(0f)

    /** Snapshot of the dragged item's layout at the moment the gesture started. Never updated
     *  during the gesture — used as a stable reference point for all displacement calculations. */
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    /** Index in the list of the item currently being dragged. Advances with each swap. */
    private var currentIndexOfDraggedItem by mutableStateOf(-1)

    val draggedItemIndex: Int get() = currentIndexOfDraggedItem

    /** Live layout info of the item at [currentIndexOfDraggedItem]. Changes after each swap
     *  because a different slot is now considered "current". */
    private val currentElement: LazyListItemInfo?
        get() = lazyListState.getVisibleItemInfoFor(currentIndexOfDraggedItem)

    /** Top and bottom pixel offsets frozen at drag start. Used as the fixed origin for
     *  [calculateBounds] so that bounds always grow from the original touch position. */
    private val initialOffsets: Pair<Int, Int>?
        get() = initiallyDraggedElement?.let { it.offset to it.offsetEnd }

    /**
     * The Y-translation (px) to apply on top of the item's current layout position so
     * that it visually tracks the finger.
     *
     * Formula: initialOffset + draggedDistance - currentLayoutOffset
     * - initialOffset + draggedDistance  = where the item would be if it moved 1:1 with the finger
     * - currentLayoutOffset              = where LazyColumn has placed the item after swaps
     * - subtracting removes the layout shift already applied, leaving only the visual delta
     *
     * Returns null when no drag is active (index = -1), so [DraggableItem] renders with
     * zero translation.
     */
    val elementDisplacement: Float?
        get() = lazyListState.getVisibleItemInfoFor(currentIndexOfDraggedItem)
            ?.let { item ->
                (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }

    /** Finds the item under the initial touch point and records it as the drag target. */
    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
            }
    }

    fun onDragInterrupted() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = -1
        initiallyDraggedElement = null
    }

    /**
     * Accumulates drag distance and triggers a swap when the dragged item's visual bounds
     * sufficiently overlap a neighbour.
     *
     * Swap condition:
     * - Dragging DOWN: visual bottom edge (`endOffset`) crosses past the neighbour's bottom edge.
     * - Dragging UP:   visual top edge (`startOffset`) crosses past the neighbour's top edge.
     *
     * `validItems` is the set of visible items that overlap the dragged item's current visual
     * bounds, excluding the dragged item itself. Only the first qualifying neighbour triggers
     * a swap per frame, keeping movement one slot at a time.
     */
    fun onDrag(offset: Offset) {
        draggedDistance += offset.y

        val topOffset = initialOffsets?.first?.toFloat() ?: return
        val (startOffset, endOffset) = calculateBounds(topOffset)
        val hoveredElement = currentElement ?: return

        val delta = startOffset - hoveredElement.offset
        val validItems = lazyListState.layoutInfo.visibleItemsInfo.filter { item ->
            !(item.offsetEnd < startOffset || item.offset > endOffset || hoveredElement.index == item.index)
        }
        val targetItem = validItems.firstOrNull {
            when {
                delta > 0 -> endOffset > hoveredElement.offsetEnd
                else -> startOffset < it.offset
            }
        } ?: return

        onMove(currentIndexOfDraggedItem, targetItem.index)
        currentIndexOfDraggedItem = targetItem.index
    }

    /**
     * Returns how many pixels the dragged item's visual bounds extend past the viewport edge.
     * A positive result means the item is past the bottom; negative means past the top.
     * Zero means no overscroll is needed.
     */
    fun computeOverscrollOffset(): Float {
        val draggedElement = initiallyDraggedElement ?: return 0f
        val (startOffset, endOffset) = calculateBounds(draggedElement.offset.toFloat())
        val diffToEnd = endOffset - lazyListState.layoutInfo.viewportEndOffset
        val diffToStart = startOffset - lazyListState.layoutInfo.viewportStartOffset
        return when {
            draggedDistance > 0 && diffToEnd > 0 -> diffToEnd
            draggedDistance < 0 && diffToStart < 0 -> diffToStart
            else -> 0f
        }
    }

    suspend fun scrollBy(offset: Float) = lazyListState.scrollBy(offset)

    /**
     * Visual bounds of the dragged item: where it appears on screen right now.
     * [topOffset] is the item's Y position at drag start; [draggedDistance] shifts it by
     * how far the finger has moved since then.
     */
    private fun calculateBounds(topOffset: Float): Pair<Float, Float> {
        val startOffset = topOffset + draggedDistance
        val endOffset = startOffset + (currentElement?.size ?: 0)
        return startOffset to endOffset
    }
}

@Composable
private fun rememberDragDropListState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit,
): ItemListDragAndDropState = remember { ItemListDragAndDropState(lazyListState, onMove) }

// ─── Gesture & overscroll ─────────────────────────────────────────────────────

/**
 * Wires [detectDragGesturesAfterLongPress] to [ItemListDragAndDropState].
 *
 * `onDragEnd` is intentionally separate from `onDragCancel`: it fires only when the user
 * lifts their finger normally, allowing the caller to commit the new order. A cancelled
 * gesture (system interrupt, incoming call) skips `onDragEnd`, leaving the ViewModel
 * untouched — `LaunchedEffect` will then restore `localItems` from the unchanged
 * external list on the next recomposition.
 */
private fun Modifier.dragGestureHandler(
    scope: CoroutineScope,
    state: ItemListDragAndDropState,
    overscrollJob: MutableState<Job?>,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {}
): Modifier = this.pointerInput(Unit) {
    detectDragGesturesAfterLongPress(
        onDragStart = { offset ->
            state.onDragStart(offset)
            onDragStart()
        },
        onDrag = { change, offset ->
            change.consume()
            state.onDrag(offset)
            handleOverscrollJob(overscrollJob, scope, state)
        },
        onDragEnd = {
            state.onDragInterrupted()
            onDragEnd()
        },
        onDragCancel = { state.onDragInterrupted() }
    )
}

/**
 * Launches a scroll coroutine when the dragged item is pushed past a viewport edge, and
 * cancels it as soon as the item returns inside the boundary. Holding a single [Job]
 * reference prevents stacking multiple concurrent scroll coroutines on rapid drag events.
 */
private fun handleOverscrollJob(
    overscrollJob: MutableState<Job?>,
    scope: CoroutineScope,
    state: ItemListDragAndDropState
) {
    if (overscrollJob.value?.isActive == true) return
    val offset = state.computeOverscrollOffset()
    if (offset != 0f) {
        overscrollJob.value = scope.launch { state.scrollBy(offset) }
    } else {
        overscrollJob.value?.cancel()
    }
}

// ─── Composable helper ────────────────────────────────────────────────────────

/**
 * Wraps item content with a Y-translation driven by [displacementOffset].
 *
 * `graphicsLayer` is used instead of `Modifier.offset` because graphicsLayer operates
 * entirely in the drawing phase — it moves the rendered pixels without affecting layout.
 * This means neighbouring items do not reflow when the dragged item moves, which is
 * exactly what we want: layout stays stable while the visual floats above it.
 */
@Composable
private fun DraggableItem(
    displacementOffset: Float?,
    content: @Composable (Modifier) -> Unit
) {
    content(Modifier.graphicsLayer { translationY = displacementOffset ?: 0f })
}

// ─── Extensions ──────────────────────────────────────────────────────────────

/**
 * Translates an absolute list index to a position within the visible items array.
 * [LazyListItemInfo.index] is absolute (counts from list start), but `visibleItemsInfo`
 * is a window starting at whatever item is currently scrolled to — so the first visible
 * item may have index 5, not 0. Subtracting the first visible item's index converts the
 * absolute index into the correct slot within the visible window.
 */
private fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return null
    return visibleItems.getOrNull(absoluteIndex - visibleItems.first().index)
}

/** Bottom edge of the item in viewport pixel coordinates. */
private val LazyListItemInfo.offsetEnd: Int get() = offset + size

private fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val element = removeAt(from) ?: return
    add(to, element)
}
