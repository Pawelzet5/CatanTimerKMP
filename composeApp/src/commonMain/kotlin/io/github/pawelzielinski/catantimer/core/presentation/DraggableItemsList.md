# DraggableItemsLazyColumn — Component Overview

This document explains how `DraggableItemsLazyColumn` behaves from the outside, how every piece
of its implementation works under the hood, and how to adapt it for more specific needs.

---

## What it does

`DraggableItemsLazyColumn` is a generic `LazyColumn` that lets users long-press and drag items to
reorder them. Displaced neighbours animate smoothly via `Modifier.animateItem()`. The dragged item
is visually lifted (scale + shadow) while moving. `onOrderChanged` fires exactly once when the drag
ends successfully — it does not fire on cancelled gestures.

---

## Public API

```kotlin
@Composable
fun <T> DraggableItemsLazyColumn(
    items: List<T>,                    // immutable list from your ViewModel / state
    key: (T) -> Any,                   // stable identity for each item (e.g. { it.id })
    onOrderChanged: (List<T>) -> Unit, // fires ONCE when drag ends successfully
    itemContent: @Composable (        // slot — apply the modifier to your root composable
        index: Int,                    //   live position in local list (updates during drag)
        item: T,
        modifier: Modifier
    ) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
)
```

### Critical rule for callers

Always apply the `modifier` argument from the slot to the **outermost** composable of your item.
It carries all the graphicsLayer transforms (Y-translation, scale, shadow elevation). Ignoring it
means neither the drag animation nor the neighbour displacement animation will appear.

```kotlin
DraggableItemsLazyColumn(
    items = players,
    key = { it.id },
    onOrderChanged = { onAction(Action.PlayersReordered(it)) },
    itemContent = { index, player, modifier ->
        PlayerRow(
            player = player,
            modifier = modifier   // <-- must be applied here
        )
    }
)
```

---

## How it works internally

### Coordinate system

All positions in this component are pixel offsets measured from the **top of the viewport**
(the visible portion of the list). This is the same coordinate space used by
`LazyListItemInfo.offset`. Positive Y is downward.

Every item in `LazyListItemInfo` exposes:
- `offset` — Y pixel coordinate of the item's **top** edge
- `size` — item height in pixels
- `offsetEnd` (extension) — `offset + size`, i.e. the Y coordinate of the **bottom** edge

---

### `ItemListDragAndDropState` — the state machine

This class owns all mutable drag state. Nothing outside the file touches it directly.

**Fields:**

| Field | What it holds |
|---|---|
| `draggedDistance` | Total Y pixels the finger has moved since drag started. Accumulates every frame. Positive = downward. |
| `initiallyDraggedElement` | Frozen snapshot of the dragged item's `LazyListItemInfo` at the exact moment the gesture began. Never updated during the drag. |
| `currentIndexOfDraggedItem` | The list index the dragged item currently occupies. Advances by one each time a swap happens. |

**Why `initiallyDraggedElement` is frozen:**  
It serves as the stable origin for all position calculations. Every formula asks "how far has the
item moved from where it *started*?" — that requires a fixed reference that does not change as
swaps shift the item's layout slot around.

---

### `elementDisplacement` — how the visual offset is derived

`elementDisplacement` is the Y-translation applied to the dragged item's visual via `graphicsLayer`.
It is the answer to: *"how far must I move the item's pixels, relative to its current layout slot,
to place it directly under the finger?"*

**Formula:**
```
displacement = initiallyDraggedElement.offset + draggedDistance - currentElement.offset
```

Step by step:
1. `initiallyDraggedElement.offset` — where the item's top edge was at drag start (e.g. Y = 100px)
2. `+ draggedDistance` — add how far the finger has moved (e.g. +80px) → 180px
   This is where the item *would* be if it moved 1:1 with the finger with no swaps.
3. `- currentElement.offset` — subtract where LazyColumn has *actually* placed the item after
   any swaps that have happened (e.g. after one swap the item slot moved to Y = 160px) → 180 - 160 = 20px
4. Result: the item's layout position already accounts for 60px of the 80px movement (due to the swap),
   so only 20px of additional visual translation is needed to put it under the finger.

**Without any swaps** (item is still in its original slot, `currentElement.offset` = 100px):
```
displacement = 100 + 80 - 100 = 80px
```
The item translates exactly as far as the finger has moved — full 1:1 tracking.

**After a swap** (item moved one slot, `currentElement.offset` = 160px):
```
displacement = 100 + 80 - 160 = 20px
```
The layout shift already did most of the work; only 20px of visual correction is needed.

This is why the formula keeps the drag feeling smooth even as swaps happen in the background.

---

### `calculateBounds` — where the dragged item visually is right now

```
startOffset = initiallyDraggedElement.offset + draggedDistance
endOffset   = startOffset + currentElement.size
```

This computes the dragged item's current **visual** bounding box (top and bottom edges),
independent of where LazyColumn has placed it in layout. These bounds are what the swap detection
and overscroll detection both use.

---

### `onDrag` — swap detection

Each pointer event frame:
1. `draggedDistance` is incremented by the delta from the hardware event.
2. `calculateBounds` recomputes the visual bounding box from the new `draggedDistance`.
3. `validItems` collects all visible items whose layout rectangles overlap the dragged item's
   visual rectangle — minus the dragged item itself. These are the candidates for a swap.
4. A swap is triggered when:
   - **Dragging down** (`delta > 0`): `endOffset > hoveredElement.offsetEnd`
     The dragged item's visual bottom has crossed past the current slot's bottom. That means the
     item has moved far enough down to "belong" in the next slot.
   - **Dragging up** (`delta < 0`): `startOffset < candidate.offset`
     The dragged item's visual top has crossed past a neighbour's top edge going upward.
5. When a swap triggers: `onMove(currentIndex, targetIndex)` is called, which calls
   `localItems.move(from, to)` — a single O(1) list mutation. Then `currentIndexOfDraggedItem`
   is updated to the new slot.

Only the first qualifying item triggers a swap per frame, keeping movement one slot at a time.

---

### `getVisibleItemInfoFor` — why the index arithmetic is needed

`LazyListItemInfo.index` is an **absolute** index (counts from the start of the entire list).
But `visibleItemsInfo` is a **window** — it only contains items currently rendered. If the list
is scrolled so item 7 is at the top, `visibleItemsInfo[0].index == 7`, not 0.

To find the `LazyListItemInfo` for absolute index 9 in that scrolled state:
```
arrayPosition = 9 - 7 = 2   →   visibleItemsInfo[2]
```
Subtracting the first visible item's index converts an absolute index into the correct position
within the visible window.

---

### `computeOverscrollOffset` — auto-scroll when dragging past edges

```
diffToEnd   = endOffset - viewportEndOffset      // how far past the bottom edge
diffToStart = startOffset - viewportStartOffset  // how far past the top edge (negative = past)
```

If `diffToEnd > 0` and the drag is downward, the item extends past the visible bottom — return
`diffToEnd` as the scroll amount. If `diffToStart < 0` and the drag is upward, the item is past
the top — return `diffToStart` (negative, so `scrollBy` scrolls upward).

---

### `DraggableItem` — why `graphicsLayer` and not `Modifier.offset`

`Modifier.offset` moves an item in the **layout phase** — neighbours see a different position and
reflow to avoid overlap. That is not what drag-and-drop needs.

`graphicsLayer { translationY }` moves the item only in the **drawing phase** — the pixels shift
but the layout slot is unchanged. Neighbours stay exactly where they are; the dragged item floats
visually above the list without disturbing the layout calculations that drive the swap detection.

---

### `dragGestureHandler` — `onDragEnd` vs `onDragCancel`

`detectDragGesturesAfterLongPress` distinguishes between a user lifting their finger
(`onDragEnd`) and the system aborting the gesture (`onDragCancel`, e.g. incoming call).

Only `onDragEnd` triggers `onOrderChanged`. If the gesture is cancelled, the ViewModel never
receives the new order — its `StateFlow` still holds the original list. On the next recomposition,
`LaunchedEffect(items)` syncs `localItems` back from that unchanged external list, effectively
reverting the in-progress drag state.

---

### `handleOverscrollJob` — why a single `Job` is held

On every drag frame, if the item is past a viewport edge, a new coroutine calling
`lazyListState.scrollBy(offset)` needs to run. Naively launching a new coroutine every frame
would stack many concurrent scrolls, causing erratic acceleration.

Holding a single `Job?` reference prevents this: if a scroll is already active (`isActive == true`),
the function returns immediately. When the item moves back inside the boundary,
`offset` becomes 0 and the job is cancelled. A fresh job starts only after the previous one is done.

---

### `animateItem` — why it must be captured inside `items {}`

`animateItem()` is a `LazyItemScope` extension on `Modifier`. `LazyItemScope` is the implicit
receiver inside the `items { ... }` lambda — it is not a composition local and does not propagate
into nested composables or lambdas passed as arguments.

Capturing it as a `Modifier` value while still inside `items {}` lets it be passed down to
`itemContent` without losing its `LazyItemScope` context:

```kotlin
items(localItems, key = key) { item ->           // LazyItemScope is here
    val animatePlacementModifier =
        if (!isDragged) Modifier.animateItem()   // captured here
        else Modifier

    DraggableItem(...) { baseModifier ->         // LazyItemScope is gone here
        itemContent(index, item, baseModifier.then(animatePlacementModifier))
    }
}
```

The dragged item is excluded because it uses `graphicsLayer` for manual placement — applying
a layout animation on top of that would cause both systems to fight over the item's position.

---

## Customising for specific cases

### Changing drag visual feedback

Scale and elevation are hardcoded. To make them configurable, add parameters to
`DraggableItemsLazyColumn`:

```kotlin
fun <T> DraggableItemsLazyColumn(
    ...
    dragScale: Float = 1.05f,
    dragShadowElevationDp: Float = 8f,
)
```

Pass them into the `graphicsLayer` block inside the `items {}` lambda.

### Drag handle (gesture limited to a specific area)

Currently any long-press anywhere on an item initiates drag. To restrict it to a handle icon,
the gesture must move off the `LazyColumn` and onto individual handle composables. The cleanest
approach is to hoist `ItemListDragAndDropState` out of the composable via a
`rememberDraggableItemsLazyColumnState()` function, pass it through the slot, and let each item
apply `dragGestureHandler` to its own handle element.

### Preventing certain items from being draggable

Add a `draggable: (T) -> Boolean` predicate. In `onDragStart`, skip items for which it returns
false:

```kotlin
.firstOrNull { item ->
    offset.y.toInt() in item.offset..(item.offset + item.size)
        && draggable(localItems[item.index])
}
```

### Horizontal drag (row reordering)

`ItemListDragAndDropState` uses Y-axis offsets throughout. A horizontal variant would need a
parallel state class operating on X offsets, wrapping a `LazyRow` instead of a `LazyColumn`.
All the core logic (bounds, swap detection, overscroll) maps directly — only the axis changes.
