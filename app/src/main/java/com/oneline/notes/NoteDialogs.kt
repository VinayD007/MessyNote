package com.oneline.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oneline.notes.ui.theme.*

// =============================================================================
// Single Note Options Bottom Sheet (Copy, Copy partially, Edit, Delete)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleNoteOptionsBottomSheet(
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onCopyPartially: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Options",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    StarIcon(
                        isFavorite = isFavorite,
                        modifier = Modifier.size(24.dp),
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
            ) {
                // 1. Copy
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopy() },
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Copy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f), thickness = 0.8.dp)

                // 2. Copy partially
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopyPartially() },
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Copy partially",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f), thickness = 0.8.dp)

                // 3. Edit
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit() },
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f), thickness = 0.8.dp)

                // 4. Delete
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() },
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = DestructiveAction,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// Partial Copy Dialog
// =============================================================================
private interface ScrollbarAdapter {
    val canScroll: Boolean
    fun thumbSizeFraction(viewportHeight: Float): Float
    fun thumbOffsetFraction(): Float
    suspend fun onDragDelta(dragDeltaPx: Float, trackHeightPx: Float, thumbHeightPx: Float)
}

private class ScrollStateAdapter(
    private val state: ScrollState
) : ScrollbarAdapter {
    override val canScroll: Boolean
        get() = state.maxValue > 0

    override fun thumbSizeFraction(viewportHeight: Float): Float {
        val total = viewportHeight + state.maxValue
        return if (total > 0f) (viewportHeight / total).coerceIn(0f, 1f) else 1f
    }

    override fun thumbOffsetFraction(): Float {
        return if (state.maxValue > 0) (state.value.toFloat() / state.maxValue).coerceIn(0f, 1f) else 0f
    }

    override suspend fun onDragDelta(dragDeltaPx: Float, trackHeightPx: Float, thumbHeightPx: Float) {
        val availableTrack = trackHeightPx - thumbHeightPx
        if (availableTrack <= 0f || state.maxValue <= 0) return
        val scrollDelta = dragDeltaPx * (state.maxValue.toFloat() / availableTrack)
        if (thumbOffsetFraction() >= 0.99f && dragDeltaPx > 0f) {
            state.scrollTo(state.maxValue)
        } else if (thumbOffsetFraction() <= 0.01f && dragDeltaPx < 0f) {
            state.scrollTo(0)
        } else {
            state.scrollBy(scrollDelta)
        }
    }
}

private class LazyListScrollbarAdapter(
    private val state: LazyListState,
    private val totalItemsCountProvider: () -> Int
) : ScrollbarAdapter {
    private val totalItemsCount: Int get() = totalItemsCountProvider()

    override val canScroll: Boolean
        get() {
            val layoutInfo = state.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty() || totalItemsCount == 0) return false
            if (visible.size < totalItemsCount) return true
            val first = visible.first()
            val last = visible.last()
            return first.offset < layoutInfo.viewportStartOffset ||
                    (last.offset + last.size) > layoutInfo.viewportEndOffset
        }

    override fun thumbSizeFraction(viewportHeight: Float): Float {
        val visible = state.layoutInfo.visibleItemsInfo
        if (visible.isEmpty() || totalItemsCount == 0) return 1f
        val avgItemSize = visible.sumOf { it.size }.toFloat() / visible.size
        val estimatedTotal = totalItemsCount * avgItemSize
        return if (estimatedTotal > 0f) (viewportHeight / maxOf(viewportHeight, estimatedTotal)).coerceIn(0f, 1f) else 1f
    }

    override fun thumbOffsetFraction(): Float {
        val visible = state.layoutInfo.visibleItemsInfo
        if (visible.isEmpty() || totalItemsCount <= 1) return 0f
        val firstIndex = state.firstVisibleItemIndex
        val firstOffset = state.firstVisibleItemScrollOffset
        val avgItemSize = visible.sumOf { it.size }.toFloat() / visible.size
        val estimatedScroll = firstIndex * avgItemSize + firstOffset
        val viewportHeight = (state.layoutInfo.viewportEndOffset - state.layoutInfo.viewportStartOffset).toFloat()
        val maxScroll = maxOf(1f, totalItemsCount * avgItemSize - viewportHeight)

        val lastItem = visible.lastOrNull()
        if (lastItem != null && lastItem.index == totalItemsCount - 1) {
            val bottomOffset = lastItem.offset + lastItem.size
            if (bottomOffset <= state.layoutInfo.viewportEndOffset) {
                return 1f
            }
        }
        if (firstIndex == 0 && firstOffset == 0) {
            return 0f
        }
        return (estimatedScroll / maxScroll).coerceIn(0f, 1f)
    }

    override suspend fun onDragDelta(dragDeltaPx: Float, trackHeightPx: Float, thumbHeightPx: Float) {
        val availableTrack = trackHeightPx - thumbHeightPx
        if (availableTrack <= 0f || totalItemsCount <= 0) return
        val visible = state.layoutInfo.visibleItemsInfo
        if (visible.isEmpty()) return
        val avgItemSize = visible.sumOf { it.size }.toFloat() / visible.size
        val viewportHeight = (state.layoutInfo.viewportEndOffset - state.layoutInfo.viewportStartOffset).toFloat()
        val maxScroll = maxOf(1f, totalItemsCount * avgItemSize - viewportHeight)
        val scrollDelta = dragDeltaPx * (maxScroll / availableTrack)

        if (thumbOffsetFraction() >= 0.99f && dragDeltaPx > 0f) {
            state.scrollToItem(totalItemsCount - 1, 0)
        } else if (thumbOffsetFraction() <= 0.01f && dragDeltaPx < 0f) {
            state.scrollToItem(0, 0)
        } else {
            state.scrollBy(scrollDelta)
        }
    }
}

@Composable
private fun DraggableScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier
) {
    if (!adapter.canScroll) return

    val coroutineScope = rememberCoroutineScope()
    var trackHeightPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val minThumbPx = with(density) { 24.dp.toPx() }

    val thumbSizeFraction = adapter.thumbSizeFraction(trackHeightPx)
    val rawThumbHeightPx = trackHeightPx * thumbSizeFraction
    val thumbHeightPx = rawThumbHeightPx.coerceIn(minThumbPx, trackHeightPx.coerceAtLeast(minThumbPx))
    val availableTrack = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
    val thumbOffsetPx = adapter.thumbOffsetFraction() * availableTrack

    Box(
        modifier = modifier
            .width(24.dp)
            .onSizeChanged { size ->
                trackHeightPx = size.height.toFloat()
            }
            .pointerInput(adapter) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                    down.consume()
                    val pointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) break
                        val deltaY = change.positionChange().y
                        change.consume()
                        if (deltaY != 0f && trackHeightPx > 0f) {
                            coroutineScope.launch {
                                adapter.onDragDelta(deltaY, trackHeightPx, thumbHeightPx)
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .padding(end = 2.dp)
                .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                .width(4.dp)
                .height(with(density) { thumbHeightPx.toDp() })
                .background(
                    color = TextSecondary.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
private data class PartialCopyPairItem(
    val pairIndex: Int,
    val fieldWords: List<Pair<Int, PartialCopyWord>>,
    val valueWords: List<Pair<Int, PartialCopyWord>>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartialCopyDialog(
    note: NoteItem,
    onDismiss: () -> Unit,
    onCopySnippet: (String) -> Unit
) {
    val isFieldValue = note.type == NoteType.FIELD_VALUE
    val isList = note.type == NoteType.LIST

    val sortedListItems = remember(note) {
        if (isList) note.getSortedListItems() else emptyList()
    }

    val pairWordItems: List<PartialCopyPairItem> = remember(note) {
        if (isFieldValue) {
            val pairs = note.getSortedFieldItems()
            var globalIdx = 0
            pairs.mapIndexed { idx, p ->
                val fWords = tokenizeForPartialCopy(p.field)
                val vWords = tokenizeForPartialCopy(p.value)
                val fItems = fWords.map { Pair(globalIdx++, it) }
                val vItems = vWords.map { Pair(globalIdx++, it) }
                PartialCopyPairItem(
                    pairIndex = idx,
                    fieldWords = fItems,
                    valueWords = vItems
                )
            }
        } else {
            emptyList()
        }
    }

    val words: List<PartialCopyWord> = remember(note, pairWordItems) {
        if (isFieldValue) {
            pairWordItems.flatMap { it.fieldWords + it.valueWords }.map { it.second }
        } else if (isList) {
            emptyList()
        } else {
            tokenizeForPartialCopy(note.text)
        }
    }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var selectionVersion by remember { mutableStateOf(0) }

    val fullText = remember(words) { words.joinToString(" ") { it.value } }
    val selectedSnippet = remember(selectionVersion, words, sortedListItems) {
        if (isList) {
            val sortedSelected = selectedIndices.sorted()
            sortedSelected.map { idx ->
                "${idx + 1}. ${sortedListItems[idx].value}"
            }.joinToString("\n")
        } else if (selectedIndices.isEmpty()) {
            ""
        } else if (selectedIndices.size == words.size) {
            fullText
        } else {
            val set = selectedIndices.toHashSet()
            val sb = StringBuilder()
            for (i in words.indices) {
                if (set.contains(i)) {
                    if (sb.isNotEmpty()) sb.append(' ')
                    sb.append(words[i].value)
                }
            }
            sb.toString()
        }
    }

    val configuration = LocalConfiguration.current
    val maxDialogHeight = configuration.screenHeightDp.dp * 0.85f

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxDialogHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Copy Partially",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = if (isList) "Tap the rows you want to copy:" else "Tap the words you want to copy:",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Interactive Word Chips Flow Layout or List Rows
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    if (isList) {
                        val listLazyState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxWidth()) {
                            LazyColumn(
                                state = listLazyState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(sortedListItems, key = { _, itm -> itm.id }) { index, item ->
                                    val isSelected = selectedIndices.contains(index)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) PrimaryBlue else SurfaceElevated,
                                        border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                if (isSelected) selectedIndices.remove(index)
                                                else selectedIndices.add(index)
                                                selectionVersion++
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}. ",
                                                color = if (isSelected) OnAccent.copy(alpha = 0.8f) else TextSecondary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = item.value,
                                                color = if (isSelected) OnAccent else TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            DraggableScrollbar(
                                adapter = remember(listLazyState, sortedListItems.size) {
                                    LazyListScrollbarAdapter(listLazyState) { sortedListItems.size }
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    } else if (isFieldValue) {
                        val fieldValueLazyListState = rememberLazyListState()
                        Box(modifier = Modifier.fillMaxWidth()) {
                            LazyColumn(
                                state = fieldValueLazyListState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(pairWordItems, key = { it.pairIndex }) { pairItem ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Static non-selectable "N." at far left
                                        Text(
                                            text = "${pairItem.pairIndex + 1}.",
                                            color = TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )

                                        // Field word chips (left-aligned)
                                        FlowRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            pairItem.fieldWords.forEach { (index, word) ->
                                                val isSelected = selectedIndices.contains(index)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) PrimaryBlue else SurfaceElevated,
                                                    border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            if (isSelected) selectedIndices.remove(index)
                                                            else selectedIndices.add(index)
                                                            selectionVersion++
                                                        }
                                                ) {
                                                    Text(
                                                        text = word.displayText,
                                                        color = if (isSelected) OnAccent else TextPrimary,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Static non-selectable "=" in the middle
                                        Text(
                                            text = "=",
                                            color = TextSecondary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        // Value word chips (right-aligned)
                                        FlowRow(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            pairItem.valueWords.forEach { (index, word) ->
                                                val isSelected = selectedIndices.contains(index)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) PrimaryBlue else SurfaceElevated,
                                                    border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            if (isSelected) selectedIndices.remove(index)
                                                            else selectedIndices.add(index)
                                                            selectionVersion++
                                                        }
                                                ) {
                                                    Text(
                                                        text = word.displayText,
                                                        color = if (isSelected) OnAccent else TextPrimary,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            DraggableScrollbar(
                                adapter = remember(fieldValueLazyListState, pairWordItems.size) {
                                    LazyListScrollbarAdapter(fieldValueLazyListState) { pairWordItems.size }
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val density = LocalDensity.current
                            val widthPx = constraints.maxWidth
                            val rows = remember(words, widthPx) {
                                val result = mutableListOf<List<Pair<Int, PartialCopyWord>>>()
                                val avgCharPx = with(density) { 8.sp.toPx() }
                                val chipExtraPx = with(density) { 26.dp.toPx() }
                                val maxRowPx = (widthPx - with(density) { 20.dp.toPx() }).coerceAtLeast(100f)

                                var currentRow = mutableListOf<Pair<Int, PartialCopyWord>>()
                                var currentRowPx = 0f

                                words.forEachIndexed { index, word ->
                                    val wordPx = word.displayText.length * avgCharPx + chipExtraPx
                                    if (currentRow.isNotEmpty() && (currentRowPx + wordPx > maxRowPx)) {
                                        result.add(currentRow)
                                        currentRow = mutableListOf()
                                        currentRowPx = 0f
                                    }
                                    currentRow.add(Pair(index, word))
                                    currentRowPx += wordPx
                                }
                                if (currentRow.isNotEmpty()) {
                                    result.add(currentRow)
                                }
                                result
                            }

                            val normalLazyListState = rememberLazyListState()
                            val haptic = LocalHapticFeedback.current

                            var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
                            val chipBounds = remember { mutableMapOf<Int, Rect>() }
                            var containerHeightPx by remember { mutableStateOf(0f) }
                            var containerWidthPx by remember { mutableStateOf(0f) }

                            var isDragSelecting by remember { mutableStateOf(false) }
                            var currentPointerPos by remember { mutableStateOf<Offset?>(null) }
                            var currentWordIndex by remember { mutableStateOf<Int?>(null) }
                            var dragAnchor by remember { mutableStateOf<Int?>(null) }
                            var dragTargetState by remember { mutableStateOf(true) }
                            var dragSnapshot by remember { mutableStateOf<Set<Int>>(emptySet()) }

                            fun updateRange(anchor: Int, current: Int, target: Boolean, snapshot: Set<Int>) {
                                val start = minOf(anchor, current)
                                val end = maxOf(anchor, current)
                                val newSelection = snapshot.toMutableSet()
                                for (i in start..end) {
                                    if (target) newSelection.add(i) else newSelection.remove(i)
                                }
                                if (selectedIndices.toSet() != newSelection) {
                                    selectedIndices.clear()
                                    selectedIndices.addAll(newSelection.sorted())
                                    selectionVersion++
                                }
                            }

                            fun findChipAt(pos: Offset): Int? {
                                val visibleItems = normalLazyListState.layoutInfo.visibleItemsInfo
                                for (item in visibleItems) {
                                    val rowWords = rows.getOrNull(item.index) ?: continue
                                    for ((index, _) in rowWords) {
                                        val rect = chipBounds[index] ?: continue
                                        if (rect.contains(pos)) {
                                            return index
                                        }
                                    }
                                }
                                return null
                            }

                            LaunchedEffect(isDragSelecting) {
                                if (!isDragSelecting) return@LaunchedEffect
                                val edgeZonePx = with(density) { 48.dp.toPx() }
                                val maxScrollSpeed = with(density) { 800.dp.toPx() }
                                val minScrollSpeed = with(density) { 150.dp.toPx() }

                                var lastFrameNanos = withFrameNanos { it }

                                while (isActive && isDragSelecting) {
                                    val frameNanos = withFrameNanos { it }
                                    val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                                    lastFrameNanos = frameNanos

                                    val pos = currentPointerPos
                                    if (pos != null && containerHeightPx > 0f) {
                                        val y = pos.y
                                        var scrollDelta = 0f

                                        if (y < edgeZonePx) {
                                            val proximity = (1f - (y.coerceAtLeast(0f) / edgeZonePx)).coerceIn(0f, 1f)
                                            val speed = minScrollSpeed + (maxScrollSpeed - minScrollSpeed) * proximity
                                            scrollDelta = -speed * dt
                                        } else if (y > containerHeightPx - edgeZonePx) {
                                            val distFromBottom = (containerHeightPx - y).coerceAtLeast(0f)
                                            val proximity = (1f - (distFromBottom / edgeZonePx)).coerceIn(0f, 1f)
                                            val speed = minScrollSpeed + (maxScrollSpeed - minScrollSpeed) * proximity
                                            scrollDelta = speed * dt
                                        }

                                        val anchor = dragAnchor
                                        if (scrollDelta < 0f && anchor != null) {
                                            val isAtStart = normalLazyListState.firstVisibleItemIndex == 0 &&
                                                    normalLazyListState.firstVisibleItemScrollOffset == 0
                                            if (!isAtStart) {
                                                normalLazyListState.scrollBy(scrollDelta)
                                                val hit = findChipAt(pos)
                                                if (hit != null && hit != currentWordIndex) {
                                                    currentWordIndex = hit
                                                    updateRange(anchor, hit, dragTargetState, dragSnapshot)
                                                }
                                            } else {
                                                if (currentWordIndex != 0) {
                                                    currentWordIndex = 0
                                                    updateRange(anchor, 0, dragTargetState, dragSnapshot)
                                                }
                                            }
                                        } else if (scrollDelta > 0f && anchor != null) {
                                            val isAtEnd = !normalLazyListState.canScrollForward
                                            if (!isAtEnd) {
                                                normalLazyListState.scrollBy(scrollDelta)
                                                val hit = findChipAt(pos)
                                                if (hit != null && hit != currentWordIndex) {
                                                    currentWordIndex = hit
                                                    updateRange(anchor, hit, dragTargetState, dragSnapshot)
                                                }
                                            } else {
                                                val lastIdx = words.size - 1
                                                if (currentWordIndex != lastIdx && lastIdx >= 0) {
                                                    currentWordIndex = lastIdx
                                                    updateRange(anchor, lastIdx, dragTargetState, dragSnapshot)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        containerCoordinates = coordinates
                                        containerHeightPx = coordinates.size.height.toFloat()
                                        containerWidthPx = coordinates.size.width.toFloat()
                                    }
                                    .pointerInput(words, rows) {
                                        coroutineScope {
                                            awaitEachGesture {
                                                val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                                                val downPos = down.position
                                                val scrollbarStripPx = with(density) { 24.dp.toPx() }
                                                if (downPos.x >= (containerWidthPx - scrollbarStripPx)) {
                                                    return@awaitEachGesture
                                                }

                                                val hitChip = findChipAt(downPos)
                                                if (hitChip == null) {
                                                    return@awaitEachGesture
                                                }

                                                val pointerId = down.id
                                                val touchSlop = viewConfiguration.touchSlop
                                                var isLongPressActive = false
                                                var currentWord = hitChip

                                                val longPressJob = launch {
                                                    delay(200L)
                                                    isLongPressActive = true
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    dragAnchor = hitChip
                                                    dragSnapshot = selectedIndices.toSet()
                                                    dragTargetState = !dragSnapshot.contains(hitChip)
                                                    currentWord = hitChip
                                                    currentWordIndex = hitChip
                                                    updateRange(hitChip, hitChip, dragTargetState, dragSnapshot)
                                                    isDragSelecting = true
                                                }

                                                try {
                                                    while (true) {
                                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                                        if (!change.pressed) {
                                                            if (!isLongPressActive && longPressJob.isActive) {
                                                                longPressJob.cancel()
                                                                change.consume()
                                                                if (selectedIndices.contains(hitChip)) {
                                                                    selectedIndices.remove(hitChip)
                                                                } else {
                                                                    selectedIndices.add(hitChip)
                                                                }
                                                                selectionVersion++
                                                            }
                                                            break
                                                        }

                                                        val dist = (change.position - downPos).getDistance()
                                                        if (!isLongPressActive) {
                                                            if (dist > touchSlop) {
                                                                longPressJob.cancel()
                                                                do {
                                                                    val ev = awaitPointerEvent(pass = PointerEventPass.Final)
                                                                } while (ev.changes.any { it.pressed })
                                                                break
                                                            }
                                                        } else {
                                                            change.consume()
                                                            currentPointerPos = change.position
                                                            val hit = findChipAt(change.position)
                                                            if (hit != null && hit != currentWord) {
                                                                currentWord = hit
                                                                currentWordIndex = hit
                                                                val anchor = dragAnchor
                                                                if (anchor != null) {
                                                                    updateRange(anchor, currentWord, dragTargetState, dragSnapshot)
                                                                }
                                                            }
                                                        }
                                                    }
                                                } finally {
                                                    longPressJob.cancel()
                                                    isDragSelecting = false
                                                    currentPointerPos = null
                                                }
                                            }
                                        }
                                    }
                            ) {
                                LazyColumn(
                                    state = normalLazyListState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(rows.size) { rowIndex ->
                                        val rowWords = rows[rowIndex]
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            rowWords.forEach { (index, word) ->
                                                val isSelected = selectedIndices.contains(index)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isSelected) PrimaryBlue else SurfaceElevated,
                                                    border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .onGloballyPositioned { coordinates ->
                                                            val parent = containerCoordinates
                                                            if (coordinates.isAttached && parent != null && parent.isAttached) {
                                                                val topLeft = parent.localPositionOf(coordinates, Offset.Zero)
                                                                chipBounds[index] = Rect(
                                                                    topLeft,
                                                                    Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                                                                )
                                                            }
                                                        }
                                                ) {
                                                    Text(
                                                        text = word.displayText,
                                                        color = if (isSelected) OnAccent else TextPrimary,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                DraggableScrollbar(
                                    adapter = remember(normalLazyListState, rows.size) {
                                        LazyListScrollbarAdapter(normalLazyListState) { rows.size }
                                    },
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .align(Alignment.CenterEnd)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Snippet Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MonospaceBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Selected Snippet:", fontSize = 11.sp, color = TextSecondary)
                            val countText = if (isList) {
                                "${selectedIndices.size} ${if (selectedIndices.size == 1) "row" else "rows"}"
                            } else {
                                "${selectedIndices.size} words"
                            }
                            Text(
                                text = "$countText (${selectedSnippet.length} chars)",
                                fontSize = 11.sp,
                                color = BrightBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val snippetScrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(snippetScrollState)
                                    .padding(end = 24.dp)
                            ) {
                                Text(
                                    text = if (selectedSnippet.isEmpty()) {
                                        if (isList) "Tap rows above to build snippet..." else "Tap words above to build snippet..."
                                    } else selectedSnippet,
                                    color = if (selectedSnippet.isEmpty()) TextMuted else TextPrimary,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                            }
                            DraggableScrollbar(
                                adapter = remember(snippetScrollState) {
                                    ScrollStateAdapter(snippetScrollState)
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterEnd)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            selectedIndices.clear()
                            if (isList) {
                                selectedIndices.addAll(sortedListItems.indices)
                            } else {
                                selectedIndices.addAll(words.indices)
                            }
                            selectionVersion++
                        }) {
                            Text("All", fontSize = 12.sp, color = BrightBlue)
                        }
                        TextButton(onClick = {
                            selectedIndices.clear()
                            selectionVersion++
                        }) {
                            Text("Clear", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Button(
                        onClick = { onCopySnippet(selectedSnippet) },
                        enabled = selectedSnippet.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_copy), contentDescription = null, tint = OnAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Snippet", color = OnAccent, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Field-Value Group Dialog (New and Edit)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScrollableSingleLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val scrollState = rememberScrollState()
    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        singleLine = true,
        textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        ),
        cursorBrush = SolidColor(BrightBlue),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = {
                    Box(modifier = Modifier.horizontalScroll(scrollState)) {
                        innerTextField()
                    }
                },
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder = {
                    Text(placeholder, color = TextMuted, fontSize = 13.sp)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = BrightBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BrightBlue
                ),
                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                    start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp
                ),
                container = {
                    @Suppress("DEPRECATION")
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = BrightBlue,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            )
        }
    )
}

@Composable
private fun FieldValueRow(
    index: Int,
    pair: FieldValueEntry,
    isLast: Boolean,
    shouldFocus: Boolean,
    showRemove: Boolean,
    onFieldChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    onAddNext: () -> Unit = {}
) {
    val fieldFocusRequester = remember { FocusRequester() }
    val valueFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            fieldFocusRequester.requestFocus()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sequential Row Number: 1., 2., etc.
        Text(
            text = "${index + 1}.",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(22.dp)
        )

        // Side-by-side: Field input
        ScrollableSingleLineTextField(
            value = pair.field,
            onValueChange = onFieldChange,
            placeholder = "Field",
            modifier = Modifier
                .weight(1f)
                .focusRequester(fieldFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { valueFocusRequester.requestFocus() }
            )
        )

        // Centered "=" with padding
        Text(
            text = "=",
            color = TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        // Side-by-side: Value input
        ScrollableSingleLineTextField(
            value = pair.value,
            onValueChange = onValueChange,
            placeholder = "Value",
            modifier = Modifier
                .weight(1f)
                .focusRequester(valueFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
        )

        // Remove X button (visible only when more than one row, otherwise invisible spacer)
        if (showRemove) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Remove pair",
                    tint = DestructiveAction,
                    modifier = Modifier.size(15.dp)
                )
            }
        } else {
            Spacer(
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 2.dp)
            )
        }
    }
}

@Composable
private fun FullScreenEditPage(
    titleContent: @Composable RowScope.() -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = BgDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close editor",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    titleContent()
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .height(40.dp)
                            .clickable(onClick = onSave)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save",
                                color = OnAccent,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
fun FieldValueGroupDialog(
    isNew: Boolean,
    initialTitle: String,
    initialItems: List<FieldValueEntry>,
    onDismiss: () -> Unit,
    onSave: (title: String, items: List<FieldValueEntry>) -> Unit
) {
    var titleText by remember { mutableStateOf(initialTitle) }
    var isEditingTitle by remember { mutableStateOf(!isNew && initialTitle.isNotBlank()) }
    val pairs = remember {
        mutableStateListOf<FieldValueEntry>().apply {
            if (initialItems.isNotEmpty()) {
                addAll(initialItems.map { it.copy() })
            } else {
                add(FieldValueEntry(field = "", value = "", insertionOrder = 0))
            }
        }
    }
    var focusTargetIndex by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val hasUnsavedContent = remember(isNew, titleText, pairs.toList(), initialTitle, initialItems) {
        if (isNew) {
            val titleNonEmpty = titleText.isNotEmpty() && titleText != "New Field Note"
            val pairsNonEmpty = pairs.any { it.field.isNotEmpty() || it.value.isNotEmpty() }
            titleNonEmpty || pairsNonEmpty
        } else {
            val titleDiffers = titleText != initialTitle
            val rowsDiffer = pairs.size != initialItems.size || pairs.indices.any { i ->
                pairs[i].field != initialItems[i].field || pairs[i].value != initialItems[i].value
            }
            titleDiffers || rowsDiffer
        }
    }

    var showDiscardConfirm by remember { mutableStateOf(false) }

    val requestClose = {
        if (hasUnsavedContent) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) {
            titleFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    fun onAddPairAction() {
        val last = pairs.lastOrNull()
        if (last != null && last.field.isBlank() && last.value.isBlank()) {
            errorMessage = "Enter a Field or Value before adding another pair"
            return
        }
        pairs.add(FieldValueEntry(field = "", value = "", insertionOrder = pairs.size))
        focusTargetIndex = pairs.lastIndex
        errorMessage = null
    }

    if (isNew) {
    AlertDialog(
        onDismissRequest = requestClose,
        containerColor = SurfaceDark,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Heading/Editable Title
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingTitle) {
                        BasicTextField(
                            value = titleText,
                            onValueChange = {
                                titleText = it
                                if (errorMessage != null) errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(titleFocusRequester),
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(BrightBlue),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (titleText.isEmpty()) {
                                        Text(
                                            text = "Title",
                                            color = TextMuted,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text(
                            text = if (titleText.isNotBlank()) titleText else (if (isNew) "New Field Note" else "Edit Field Note"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = TextPrimary,
                            modifier = Modifier.clickable {
                                isEditingTitle = true
                                if (isNew && titleText == "New Field Note") {
                                    titleText = ""
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: "+ Add" action
                TextButton(
                    onClick = { onAddPairAction() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+ Add",
                        color = BrightBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Field-Value Pairs Section
                Text(
                    text = "Field-Value Pairs",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                pairs.forEachIndexed { index, pair ->
                    FieldValueRow(
                        index = index,
                        pair = pair,
                        isLast = index == pairs.lastIndex,
                        shouldFocus = focusTargetIndex == index,
                        showRemove = pairs.size > 1,
                        onFieldChange = { newField ->
                            pairs[index] = pair.copy(field = newField)
                            if (errorMessage != null) errorMessage = null
                        },
                        onValueChange = { newVal ->
                            pairs[index] = pair.copy(value = newVal)
                            if (errorMessage != null) errorMessage = null
                        },
                        onRemove = {
                            if (pairs.size > 1) {
                                pairs.removeAt(index)
                            }
                        },
                        onAddNext = { onAddPairAction() }
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = DestructiveAction,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rawT = titleText.trim()
                    val t = if (rawT == "New Field Note" && isNew && !isEditingTitle) "" else rawT
                    val validPairs = pairs.filter { it.field.isNotBlank() || it.value.isNotBlank() }
                    if (validPairs.isEmpty()) {
                        errorMessage = "At least one Field or Value pair is required"
                        return@TextButton
                    }
                    val finalItems = validPairs.mapIndexed { idx, itm ->
                        itm.copy(
                            field = itm.field.trim(),
                            value = itm.value.trim(),
                            insertionOrder = idx
                        )
                    }
                    onSave(t, finalItems)
                }
            ) {
                Text(
                    text = "Save",
                    color = PrimaryBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = requestClose) {
                Text(
                    text = "Cancel",
                    color = BrightBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
    } else {
        BackHandler(enabled = !showDiscardConfirm, onBack = requestClose)
        FullScreenEditPage(
            onClose = requestClose,
            onSave = {
                val rawT = titleText.trim()
                val validPairs = pairs.filter { it.field.isNotBlank() || it.value.isNotBlank() }
                if (validPairs.isEmpty()) {
                    errorMessage = "At least one Field or Value pair is required"
                } else {
                    val finalItems = validPairs.mapIndexed { idx, itm ->
                        itm.copy(
                            field = itm.field.trim(),
                            value = itm.value.trim(),
                            insertionOrder = idx
                        )
                    }
                    onSave(rawT, finalItems)
                }
            },
            titleContent = {
                if (isEditingTitle) {
                    BasicTextField(
                        value = titleText,
                        onValueChange = {
                            titleText = it
                            if (errorMessage != null) errorMessage = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(titleFocusRequester),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BrightBlue),
                        decorationBox = { innerTextField ->
                            Box {
                                if (titleText.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        color = TextMuted,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(
                        text = if (titleText.isNotBlank()) titleText else "Edit Field Note",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditingTitle = true }
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Field-Value Pairs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { onAddPairAction() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ Add",
                                color = BrightBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    pairs.forEachIndexed { index, pair ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceCard,
                            border = BorderStroke(1.dp, BrightBlue)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                FieldValueRow(
                                    index = index,
                                    pair = pair,
                                    isLast = index == pairs.lastIndex,
                                    shouldFocus = focusTargetIndex == index,
                                    showRemove = pairs.size > 1,
                                    onFieldChange = { newField ->
                                        pairs[index] = pair.copy(field = newField)
                                        if (errorMessage != null) errorMessage = null
                                    },
                                    onValueChange = { newVal ->
                                        pairs[index] = pair.copy(value = newVal)
                                        if (errorMessage != null) errorMessage = null
                                    },
                                    onRemove = {
                                        if (pairs.size > 1) pairs.removeAt(index)
                                    },
                                    onAddNext = { onAddPairAction() }
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage.orEmpty(),
                            color = DestructiveAction,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        )
    }

    if (showDiscardConfirm) {
        DiscardConfirmDialog(
            onDismiss = { showDiscardConfirm = false },
            onConfirm = {
                showDiscardConfirm = false
                onDismiss()
            }
        )
    }
}

@Composable
fun NewFieldValueGroupDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, items: List<FieldValueEntry>) -> Unit
) {
    FieldValueGroupDialog(
        isNew = true,
        initialTitle = "",
        initialItems = emptyList(),
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
fun EditFieldValueGroupDialog(
    initialTitle: String,
    initialItems: List<FieldValueEntry>,
    onDismiss: () -> Unit,
    onSave: (title: String, items: List<FieldValueEntry>) -> Unit
) {
    FieldValueGroupDialog(
        isNew = false,
        initialTitle = initialTitle,
        initialItems = initialItems,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

// =============================================================================
// List Group Dialog (New and Edit)
// =============================================================================
@Composable
private fun ListRow(
    index: Int,
    item: ListRowEntry,
    isLast: Boolean,
    shouldFocus: Boolean,
    showRemove: Boolean,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
    onAddNext: () -> Unit = {}
) {
    val valueFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            valueFocusRequester.requestFocus()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading serial number ("1.", "2.", ...)
        Text(
            text = "${index + 1}.",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(26.dp)
        )

        // Single text field per row
        OutlinedTextField(
            value = item.value,
            onValueChange = onValueChange,
            placeholder = {
                Text("Item", color = TextMuted, fontSize = 13.sp)
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(valueFocusRequester),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = BrightBlue,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = BrightBlue
            ),
            singleLine = false,
            maxLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )

        // Row delete "X" hidden when only 1 row, shown when 2+ rows exist
        if (showRemove) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Remove item",
                    tint = DestructiveAction,
                    modifier = Modifier.size(15.dp)
                )
            }
        } else {
            Spacer(
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 2.dp)
            )
        }
    }
}

@Composable
fun ListGroupDialog(
    isNew: Boolean,
    initialTitle: String,
    initialItems: List<ListRowEntry>,
    onDismiss: () -> Unit,
    onSave: (title: String, items: List<ListRowEntry>) -> Unit
) {
    var titleText by remember { mutableStateOf(initialTitle) }
    var isEditingTitle by remember { mutableStateOf(!isNew && initialTitle.isNotBlank()) }
    val rows = remember {
        mutableStateListOf<ListRowEntry>().apply {
            if (initialItems.isNotEmpty()) {
                addAll(initialItems.map { it.copy() })
            } else {
                add(ListRowEntry(value = "", insertionOrder = 0))
            }
        }
    }
    var focusTargetIndex by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val hasUnsavedContent = remember(isNew, titleText, rows.toList(), initialTitle, initialItems) {
        if (isNew) {
            val titleNonEmpty = titleText.isNotEmpty() && titleText != "New List"
            val rowsNonEmpty = rows.any { it.value.isNotEmpty() }
            titleNonEmpty || rowsNonEmpty
        } else {
            val titleDiffers = titleText != initialTitle
            val rowsDiffer = rows.size != initialItems.size || rows.indices.any { i ->
                rows[i].value != initialItems[i].value
            }
            titleDiffers || rowsDiffer
        }
    }

    var showDiscardConfirm by remember { mutableStateOf(false) }

    val requestClose = {
        if (hasUnsavedContent) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) {
            titleFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val canAddRow = rows.lastOrNull()?.value?.isNotBlank() == true

    fun onAddRowAction() {
        if (!canAddRow) {
            errorMessage = "Fill the current row before adding another"
            return
        }
        rows.add(ListRowEntry(value = "", insertionOrder = rows.size))
        focusTargetIndex = rows.lastIndex
        errorMessage = null
    }

    if (isNew) {
    AlertDialog(
        onDismissRequest = requestClose,
        containerColor = SurfaceDark,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Heading/Editable Title
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingTitle) {
                        BasicTextField(
                            value = titleText,
                            onValueChange = {
                                titleText = it
                                if (errorMessage != null) errorMessage = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(titleFocusRequester),
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(BrightBlue),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (titleText.isEmpty()) {
                                        Text(
                                            text = "Title",
                                            color = TextMuted,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text(
                            text = if (titleText.isNotBlank()) titleText else (if (isNew) "New List" else "Edit List"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = TextPrimary,
                            modifier = Modifier.clickable {
                                isEditingTitle = true
                                if (isNew && titleText == "New List") {
                                    titleText = ""
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: "+ Add" action (disabled or no-ops if last row is empty)
                TextButton(
                    onClick = { onAddRowAction() },
                    enabled = canAddRow,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+ Add",
                        color = if (canAddRow) BrightBlue else TextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "List Items",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rows.forEachIndexed { index, rowItem ->
                        ListRow(
                            index = index,
                            item = rowItem,
                            isLast = index == rows.lastIndex,
                            shouldFocus = focusTargetIndex == index,
                            showRemove = rows.size > 1,
                            onValueChange = { newVal ->
                                rows[index] = rowItem.copy(value = newVal)
                                if (errorMessage != null) errorMessage = null
                            },
                            onRemove = {
                                if (rows.size > 1) {
                                    rows.removeAt(index)
                                }
                            },
                            onAddNext = { onAddRowAction() }
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = DestructiveAction,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (rows.isEmpty() || rows.first().value.isBlank()) {
                        errorMessage = "Row 1 cannot be empty"
                        return@TextButton
                    }
                    var lastNonEmpty = rows.size - 1
                    while (lastNonEmpty >= 0 && rows[lastNonEmpty].value.isBlank()) {
                        lastNonEmpty--
                    }
                    if (lastNonEmpty < 0) {
                        errorMessage = "At least one item is required"
                        return@TextButton
                    }
                    val validRows = rows.subList(0, lastNonEmpty + 1).mapIndexed { idx, itm ->
                        itm.copy(value = itm.value.trim(), insertionOrder = idx)
                    }
                    val rawT = titleText.trim()
                    val t = if (rawT == "New List" && isNew && !isEditingTitle) "" else rawT
                    onSave(t, validRows)
                }
            ) {
                Text(
                    text = "Save",
                    color = PrimaryBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = requestClose) {
                Text(
                    text = "Cancel",
                    color = BrightBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
    } else {
        BackHandler(enabled = !showDiscardConfirm, onBack = requestClose)
        FullScreenEditPage(
            onClose = requestClose,
            onSave = {
                if (rows.isEmpty() || rows.first().value.isBlank()) {
                    errorMessage = "Row 1 cannot be empty"
                } else {
                    var lastNonEmpty = rows.size - 1
                    while (lastNonEmpty >= 0 && rows[lastNonEmpty].value.isBlank()) {
                        lastNonEmpty--
                    }
                    if (lastNonEmpty < 0) {
                        errorMessage = "At least one item is required"
                    } else {
                        val validRows = rows.subList(0, lastNonEmpty + 1).mapIndexed { idx, itm ->
                            itm.copy(value = itm.value.trim(), insertionOrder = idx)
                        }
                        onSave(titleText.trim(), validRows)
                    }
                }
            },
            titleContent = {
                if (isEditingTitle) {
                    BasicTextField(
                        value = titleText,
                        onValueChange = {
                            titleText = it
                            if (errorMessage != null) errorMessage = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(titleFocusRequester),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(BrightBlue),
                        decorationBox = { innerTextField ->
                            Box {
                                if (titleText.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        color = TextMuted,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(
                        text = if (titleText.isNotBlank()) titleText else "Edit List",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditingTitle = true }
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "List Items",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { onAddRowAction() },
                            enabled = canAddRow,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ Add",
                                color = if (canAddRow) BrightBlue else TextMuted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rows.forEachIndexed { index, rowItem ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceCard,
                                border = BorderStroke(1.dp, BrightBlue)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                    ListRow(
                                        index = index,
                                        item = rowItem,
                                        isLast = index == rows.lastIndex,
                                        shouldFocus = focusTargetIndex == index,
                                        showRemove = rows.size > 1,
                                        onValueChange = { newVal ->
                                            rows[index] = rowItem.copy(value = newVal)
                                            if (errorMessage != null) errorMessage = null
                                        },
                                        onRemove = {
                                            if (rows.size > 1) rows.removeAt(index)
                                        },
                                        onAddNext = { onAddRowAction() }
                                    )
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage.orEmpty(),
                            color = DestructiveAction,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        )
    }

    if (showDiscardConfirm) {
        DiscardConfirmDialog(
            onDismiss = { showDiscardConfirm = false },
            onConfirm = {
                showDiscardConfirm = false
                onDismiss()
            }
        )
    }
}

@Composable
fun NewListGroupDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, items: List<ListRowEntry>) -> Unit
) {
    ListGroupDialog(
        isNew = true,
        initialTitle = "",
        initialItems = emptyList(),
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
fun EditListGroupDialog(
    initialTitle: String,
    initialItems: List<ListRowEntry>,
    onDismiss: () -> Unit,
    onSave: (title: String, items: List<ListRowEntry>) -> Unit
) {
    ListGroupDialog(
        isNew = false,
        initialTitle = initialTitle,
        initialItems = initialItems,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

// =============================================================================
// Edit Note Dialog
// =============================================================================
@Composable
fun EditNoteDialog(
    note: NoteItem,
    onDismiss: () -> Unit,
    onSave: (trimmedText: String, spans: List<TextSpan>) -> Unit
) {
    var textFieldValue by remember(note.id, note.updatedAt) {
        mutableStateOf(
            TextFieldValue(
                text = note.text,
                selection = TextRange(note.text.length)
            )
        )
    }
    var currentSpans by remember(note.id, note.updatedAt) {
        mutableStateOf(note.spans.toList())
    }

    val hasUnsavedContent = remember(textFieldValue.text, currentSpans, note.text, note.spans) {
        textFieldValue.text != note.text || currentSpans != note.spans.toList()
    }

    var showDiscardConfirm by remember { mutableStateOf(false) }

    val requestClose = {
        if (hasUnsavedContent) {
            showDiscardConfirm = true
        } else {
            onDismiss()
        }
    }

    BackHandler(enabled = !showDiscardConfirm, onBack = requestClose)
    FullScreenEditPage(
        onClose = requestClose,
        onSave = {
            val trimmed = textFieldValue.text.trim()
            if (trimmed.isNotEmpty()) {
                onSave(trimmed, currentSpans)
            }
        },
        titleContent = {
            Text(
                text = "Edit Message",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
        },
        content = {
            val editView = LocalView.current
            val currentEditValue = rememberUpdatedState(textFieldValue)
            val currentEditSpans = rememberUpdatedState(currentSpans)

            val editDialogTextToolbar = remember(editView) {
                MonoTextToolbar(
                    view = editView,
                    onMonoRequested = {
                        val valNow = currentEditValue.value
                        val sel = valNow.selection
                        if (!sel.collapsed && sel.length > 0) {
                            currentSpans = toggleMonospaceSpan(
                                valNow.text.length,
                                currentEditSpans.value,
                                sel.min,
                                sel.max
                            )
                            textFieldValue = valNow.copy(selection = sel)
                        }
                    },
                    canApplyMono = {
                        val sel = currentEditValue.value.selection
                        !sel.collapsed && sel.length > 0
                    }
                )
            }

            val currentMonoText = MonospaceText
            val currentMonoBg = MonospaceBg

            CompositionLocalProvider(LocalTextToolbar provides editDialogTextToolbar) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { newVal ->
                            val oldText = textFieldValue.text
                            val newText = newVal.text
                            if (oldText != newText) {
                                currentSpans = adjustSpansForTextChange(oldText, newText, currentSpans)
                            }
                            textFieldValue = newVal
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        visualTransformation = remember(currentSpans, currentMonoText, currentMonoBg) {
                            VisualTransformation { text ->
                                TransformedText(
                                    buildMonospaceAnnotatedString(text.text, currentSpans, monoColor = currentMonoText, monoBackground = currentMonoBg),
                                    OffsetMapping.Identity
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = BrightBlue,
                            unfocusedBorderColor = BrightBlue,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = BrightBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (currentSpans.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Monospace Preview:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MonospaceBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = buildMonospaceAnnotatedString(textFieldValue.text, currentSpans, monoColor = MonospaceText, monoBackground = MonospaceBg),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    )

    if (showDiscardConfirm) {
        DiscardConfirmDialog(
            onDismiss = { showDiscardConfirm = false },
            onConfirm = {
                showDiscardConfirm = false
                onDismiss()
            }
        )
    }
}

// =============================================================================
// Delete Confirmation Dialog (Unified for Single & Multi-Selection)
// =============================================================================
@Composable
fun DeleteConfirmDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = if (count == 1) "Delete 1 note?" else "Delete $count notes?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = if (count == 1) "Are you sure you want to delete this note?" else "Are you sure you want to delete these $count notes?",
                color = TextSecondary,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    color = DestructiveAction,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

// =============================================================================
// Discard Confirmation Dialog
// =============================================================================
@Composable
fun DiscardConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Discard changes?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = TextPrimary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Confirm",
                    color = BrightBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = BrightBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
