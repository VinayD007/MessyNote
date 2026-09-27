package com.oneline.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oneline.notes.ui.theme.*

@Suppress("DEPRECATION")
@Composable
fun SentNoteBubble(
    note: NoteItem,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMonoTap: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = isSelected
) {
    val currentMonoText = MonospaceText
    val currentMonoBg = MonospaceBg
    val accentColor = BrightBlue
    val annotatedText = remember(note.text, note.spans.toList(), currentMonoText, currentMonoBg) {
        buildMonospaceAnnotatedString(note.text, note.spans, monoColor = currentMonoText, monoBackground = currentMonoBg)
    }

    val bubbleScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bubbleSelectionScale"
    )
    val bubbleBgColor by animateColorAsState(
        targetValue = if (isSelected) SelectedItemBackground else SurfaceCard,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bubbleBgColor"
    )
    val bubbleBorderColor by animateColorAsState(
        targetValue = if (isSelected) BrightBlue.copy(alpha = 0.7f) else BorderSubtle.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bubbleBorderColor"
    )
    val isExpandableNote = note.type == NoteType.NORMAL || note.type == NoteType.LIST
    val inSelectionMode = isSelected || isSelectionMode
    var isExpanded by rememberSaveable(note.text, key = note.id) {
        mutableStateOf(false)
    }
    var isLongNote by remember(note.id, note.text) {
        mutableStateOf(false)
    }
    val collapseButtonSpacing = 8.dp
    val collapseGutter = ActionButtonSize + collapseButtonSpacing
    val rightGutter = if (isExpandableNote) collapseGutter else 0.dp

    val bubbleShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleBgColor,
            border = BorderStroke(1.dp, bubbleBorderColor),
            shadowElevation = if (isSelected) 4.dp else 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .then(
                    if (isExpandableNote) {
                        Modifier.padding(end = rightGutter)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
                .widthIn(min = 80.dp)
                .graphicsLayer {
                    scaleX = bubbleScale
                    scaleY = bubbleScale
                }
                .clip(bubbleShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
            if (note.type == NoteType.FIELD_VALUE) {
                if (!note.title.isNullOrBlank()) {
                    Text(
                        text = note.title ?: "",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    HorizontalDivider(
                        color = BorderSubtle.copy(alpha = 0.5f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val sortedItems = note.getSortedFieldItems()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sortedItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = item.field,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Start
                                )
                            }

                            Text(
                                text = "=",
                                color = TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (item.value.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MonospaceBg,
                                        modifier = Modifier.clickable {
                                            onMonoTap(item.value)
                                        }
                                    ) {
                                        Text(
                                            text = item.value,
                                            fontFamily = FontFamily.Monospace,
                                            color = MonospaceText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (note.type == NoteType.LIST) {
                if (!note.title.isNullOrBlank()) {
                    Text(
                        text = note.title ?: "",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    HorizontalDivider(
                        color = BorderSubtle.copy(alpha = 0.5f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                val sortedItems = remember(note.listItems.toList()) {
                    note.getSortedListItems()
                }
                val rawRows = remember(sortedItems, note.text) {
                    if (sortedItems.isNotEmpty()) {
                        sortedItems.mapIndexed { idx, it -> "${idx + 1}. ${it.value}" }
                    } else if (note.text.isNotBlank()) {
                        note.text.lines().filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }
                }

                val rowAnnotatedStrings = remember(rawRows, note.spans.toList(), currentMonoText, currentMonoBg) {
                    var currentOffset = 0
                    rawRows.map { rowStr ->
                        val rowStart = currentOffset
                        val rowEnd = rowStart + rowStr.length
                        currentOffset = rowEnd + 1
                        val rowSpans = note.spans.mapNotNull { span ->
                            val s = maxOf(span.start, rowStart)
                            val e = minOf(span.end, rowEnd)
                            if (s < e) TextSpan(s - rowStart, e - rowStart, span.type) else null
                        }
                        ListRowData(
                            rawText = rowStr,
                            annotatedText = buildMonospaceAnnotatedString(rowStr, rowSpans, monoColor = currentMonoText, monoBackground = currentMonoBg),
                            spans = rowSpans
                        )
                    }
                }

                val textMeasurer = rememberTextMeasurer()

                val textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )

                BoxWithConstraints {
                    val maxWidthPx = if (constraints.hasBoundedWidth && constraints.maxWidth > 0) {
                        constraints.maxWidth
                    } else {
                        Constraints.Infinity
                    }
                    val textConstraints = if (maxWidthPx != Constraints.Infinity) Constraints(maxWidth = maxWidthPx) else Constraints()

                    val listTruncationData = remember(
                        rowAnnotatedStrings,
                        maxWidthPx,
                        currentMonoText,
                        currentMonoBg,
                        accentColor,
                        textStyle
                    ) {
                        computeListTruncation(
                            rows = rowAnnotatedStrings,
                            textMeasurer = textMeasurer,
                            textStyle = textStyle,
                            constraints = textConstraints,
                            monoColor = currentMonoText,
                            monoBg = currentMonoBg,
                            accentColor = accentColor
                        )
                    }

                    SideEffect {
                        if (isLongNote != listTruncationData.isLong) {
                            isLongNote = listTruncationData.isLong
                        }
                    }

                    val displayRows = if (isExpanded) {
                        listTruncationData.expandedRows
                    } else {
                        listTruncationData.collapsedRows
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        displayRows.forEach { rowData ->
                            ClickableText(
                                text = rowData.annotatedText,
                                style = textStyle,
                                onClick = { offset ->
                                    val isTruncatedRowWithMore = !isExpanded && listTruncationData.isLong &&
                                        (offset >= rowData.rawText.length || rowData.annotatedText.getStringAnnotations(tag = "more", start = offset, end = offset).isNotEmpty())
                                    if (isTruncatedRowWithMore) {
                                        if (inSelectionMode) {
                                            onClick()
                                        } else {
                                            isExpanded = true
                                        }
                                        return@ClickableText
                                    }

                                    val isExpandedRowWithLess = isExpanded && listTruncationData.isLong &&
                                        (offset >= rowData.rawText.length || rowData.annotatedText.getStringAnnotations(tag = "less", start = offset, end = offset).isNotEmpty())
                                    if (isExpandedRowWithLess) {
                                        if (inSelectionMode) {
                                            onClick()
                                        } else {
                                            isExpanded = false
                                        }
                                        return@ClickableText
                                    }

                                    val clickedSpan = rowData.spans.firstOrNull { offset in it.start until it.end }
                                    if (clickedSpan != null) {
                                        val spanText = rowData.rawText.substring(
                                            clickedSpan.start.coerceIn(0, rowData.rawText.length),
                                            clickedSpan.end.coerceIn(0, rowData.rawText.length)
                                        )
                                        onMonoTap(spanText)
                                        return@ClickableText
                                    }

                                    onClick()
                                }
                            )
                        }
                    }
                }
            } else {
                val textMeasurer = rememberTextMeasurer()

                val textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )

                BoxWithConstraints {
                    val maxWidthPx = if (constraints.hasBoundedWidth && constraints.maxWidth > 0) {
                        constraints.maxWidth
                    } else {
                        Constraints.Infinity
                    }
                    val textConstraints = if (maxWidthPx != Constraints.Infinity) Constraints(maxWidth = maxWidthPx) else Constraints()

                    val truncationData = remember(
                        note.text,
                        note.spans.toList(),
                        maxWidthPx,
                        currentMonoText,
                        currentMonoBg,
                        accentColor,
                        textStyle
                    ) {
                        val fullLayoutResult = textMeasurer.measure(
                            text = annotatedText,
                            style = textStyle,
                            constraints = textConstraints
                        )
                        val isLong = fullLayoutResult.lineCount > 4
                        if (!isLong) {
                            TruncationResult(
                                isLong = false,
                                bestCut = note.text.length,
                                collapsedText = annotatedText,
                                visibleSpans = note.spans
                            )
                        } else {
                            val maxLineEnd = fullLayoutResult.getLineEnd(lineIndex = 3, visibleEnd = true).coerceIn(0, note.text.length)
                            fun fitsIn4Lines(cut: Int): Boolean {
                                val (candidate, _) = buildCollapsedText(note, cut, currentMonoText, currentMonoBg, accentColor)
                                val measured = textMeasurer.measure(
                                    text = candidate,
                                    style = textStyle,
                                    constraints = textConstraints
                                )
                                return measured.lineCount <= 4
                            }

                            var bestCut = 0
                            if (fitsIn4Lines(maxLineEnd)) {
                                bestCut = maxLineEnd
                            } else {
                                var low = 0
                                var high = maxLineEnd
                                while (low <= high) {
                                    val mid = (low + high) / 2
                                    val adjustedMid = if (mid > 0 && mid < note.text.length &&
                                        note.text[mid - 1].isHighSurrogate() && note.text[mid].isLowSurrogate()
                                    ) {
                                        mid - 1
                                    } else {
                                        mid
                                    }

                                    if (fitsIn4Lines(adjustedMid)) {
                                        bestCut = adjustedMid
                                        low = mid + 1
                                    } else {
                                        high = mid - 1
                                    }
                                }
                            }
                            val (collapsedText, visibleSpans) = buildCollapsedText(note, bestCut, currentMonoText, currentMonoBg, accentColor)
                            TruncationResult(
                                isLong = true,
                                bestCut = bestCut,
                                collapsedText = collapsedText,
                                visibleSpans = visibleSpans
                            )
                        }
                    }

                    SideEffect {
                        if (isLongNote != truncationData.isLong) {
                            isLongNote = truncationData.isLong
                        }
                    }

                    val expandedAnnotatedText = remember(annotatedText, accentColor) {
                        buildAnnotatedString {
                            append(annotatedText)
                            val separator = if (note.text.endsWith(" ") || note.text.endsWith("\n")) "" else " "
                            append(separator)
                            pushStringAnnotation(tag = "action", annotation = "less")
                            pushStringAnnotation(tag = "less", annotation = "less")
                            withStyle(SpanStyle(color = accentColor, fontSize = 15.sp)) {
                                append("less")
                            }
                            pop()
                        }
                    }

                    val displayedText = when {
                        !truncationData.isLong -> annotatedText
                        isExpanded -> expandedAnnotatedText
                        else -> truncationData.collapsedText
                    }

                    ClickableText(
                        text = displayedText,
                        modifier = Modifier,
                        onClick = { offset ->
                            if (truncationData.isLong && !isExpanded) {
                                if (offset >= truncationData.bestCut) {
                                    if (inSelectionMode) {
                                        onClick()
                                    } else {
                                        isExpanded = true
                                    }
                                } else {
                                    val clickedSpan = truncationData.visibleSpans.firstOrNull { offset in it.start until it.end }
                                    if (clickedSpan != null) {
                                        val spanText = note.text.substring(
                                            clickedSpan.start.coerceIn(0, truncationData.bestCut),
                                            clickedSpan.end.coerceIn(0, truncationData.bestCut)
                                        )
                                        onMonoTap(spanText)
                                    } else {
                                        onClick()
                                    }
                                }
                            } else if (truncationData.isLong && isExpanded) {
                                if (offset >= note.text.length) {
                                    if (inSelectionMode) {
                                        onClick()
                                    } else {
                                        isExpanded = false
                                    }
                                } else {
                                    val clickedSpan = note.spans.firstOrNull { offset in it.start until it.end }
                                    if (clickedSpan != null) {
                                        val spanText = note.text.substring(
                                            clickedSpan.start.coerceIn(0, note.text.length),
                                            clickedSpan.end.coerceIn(0, note.text.length)
                                        )
                                        onMonoTap(spanText)
                                    } else {
                                        onClick()
                                    }
                                }
                            } else {
                                val clickedSpan = note.spans.firstOrNull { offset in it.start until it.end }
                                if (clickedSpan != null) {
                                    val spanText = note.text.substring(
                                        clickedSpan.start.coerceIn(0, note.text.length),
                                        clickedSpan.end.coerceIn(0, note.text.length)
                                    )
                                    onMonoTap(spanText)
                                } else {
                                    onClick()
                                }
                            }
                        },
                        style = textStyle
                    )

                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.favorite) {
                    StarIcon(
                        isFavorite = true,
                        modifier = Modifier.size(11.dp),
                        contentDescription = "Favorite"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = formatTime(note.createdAt),
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End
                )
            }
        }
    }

    if (isExpandableNote) {
        AnimatedVisibility(
            visible = isExpanded && isLongNote && !inSelectionMode,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150)),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .size(ActionButtonSize)
                    .clip(CircleShape)
                    .clickable { isExpanded = false }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = KeyboardArrowUpIcon,
                        contentDescription = "Collapse",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
}

private data class TruncationResult(
    val isLong: Boolean,
    val bestCut: Int,
    val collapsedText: AnnotatedString,
    val visibleSpans: List<TextSpan>
)

private fun buildCollapsedText(
    note: NoteItem,
    cutIndex: Int,
    monoColor: Color,
    monoBg: Color,
    accentColor: Color
): Pair<AnnotatedString, List<TextSpan>> {
    val safeCut = cutIndex.coerceIn(0, note.text.length)
    val baseText = note.text.substring(0, safeCut)
    val visibleSpans = note.spans.mapNotNull { span ->
        if (span.start >= safeCut) null
        else {
            val clippedEnd = minOf(span.end, safeCut)
            if (span.start < clippedEnd) TextSpan(span.start, clippedEnd, span.type) else null
        }
    }
    val annotated = buildAnnotatedString {
        append(buildMonospaceAnnotatedString(baseText, visibleSpans, monoColor = monoColor, monoBackground = monoBg))
        append("… ")
        pushStringAnnotation(tag = "action", annotation = "more")
        pushStringAnnotation(tag = "more", annotation = "more")
        withStyle(SpanStyle(color = accentColor, fontSize = 15.sp)) {
            append("more")
        }
        pop()
    }
    return Pair(annotated, visibleSpans)
}

private val KeyboardArrowUpIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "KeyboardArrowUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(7.41f, 15.41f)
            lineTo(12f, 10.83f)
            lineTo(16.59f, 15.41f)
            lineTo(18f, 14f)
            lineTo(12f, 8f)
            lineTo(6f, 14f)
            close()
        }
    }.build()
}

private data class ListRowData(
    val rawText: String,
    val annotatedText: AnnotatedString,
    val spans: List<TextSpan>
)

private data class ListTruncationResult(
    val isLong: Boolean,
    val collapsedRows: List<ListRowData>,
    val expandedRows: List<ListRowData>
)

private fun computeListTruncation(
    rows: List<ListRowData>,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    constraints: Constraints,
    monoColor: Color,
    monoBg: Color,
    accentColor: Color
): ListTruncationResult {
    if (rows.isEmpty()) {
        return ListTruncationResult(
            isLong = false,
            collapsedRows = emptyList(),
            expandedRows = emptyList()
        )
    }

    val rowLineCounts = rows.map { row ->
        val layout = textMeasurer.measure(
            text = row.annotatedText,
            style = textStyle,
            constraints = constraints
        )
        layout.lineCount
    }
    val totalLines = rowLineCounts.sum()
    val isLong = totalLines > 4

    val expandedRows = if (!isLong) {
        rows
    } else {
        val lastIdx = rows.lastIndex
        rows.mapIndexed { idx, row ->
            if (idx == lastIdx) {
                val lessText = buildAnnotatedString {
                    append(row.annotatedText)
                    val separator = if (row.rawText.endsWith(" ") || row.rawText.endsWith("\n")) "" else " "
                    append(separator)
                    pushStringAnnotation(tag = "action", annotation = "less")
                    pushStringAnnotation(tag = "less", annotation = "less")
                    withStyle(SpanStyle(color = accentColor, fontSize = 15.sp)) {
                        append("less")
                    }
                    pop()
                }
                ListRowData(row.rawText, lessText, row.spans)
            } else {
                row
            }
        }
    }

    if (!isLong) {
        return ListTruncationResult(
            isLong = false,
            collapsedRows = rows,
            expandedRows = expandedRows
        )
    }

    val collapsedRows = mutableListOf<ListRowData>()
    var linesRemaining = 4

    for (i in rows.indices) {
        val row = rows[i]
        val lineCount = rowLineCounts[i]
        val isLastInList = i == rows.lastIndex

        if (!isLastInList && lineCount < linesRemaining) {
            collapsedRows.add(row)
            linesRemaining -= lineCount
        } else {
            val truncatedRow = truncateListRowWithMore(
                row = row,
                maxLines = linesRemaining,
                textMeasurer = textMeasurer,
                textStyle = textStyle,
                constraints = constraints,
                monoColor = monoColor,
                monoBg = monoBg,
                accentColor = accentColor
            )
            collapsedRows.add(truncatedRow)
            break
        }
    }

    return ListTruncationResult(
        isLong = true,
        collapsedRows = collapsedRows,
        expandedRows = expandedRows
    )
}

private fun truncateListRowWithMore(
    row: ListRowData,
    maxLines: Int,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    constraints: Constraints,
    monoColor: Color,
    monoBg: Color,
    accentColor: Color
): ListRowData {
    val rawText = row.rawText
    val spans = row.spans

    fun buildCandidate(cut: Int): Pair<AnnotatedString, List<TextSpan>> {
        val safeCut = cut.coerceIn(0, rawText.length)
        val baseText = rawText.substring(0, safeCut)
        val visibleSpans = spans.mapNotNull { span ->
            if (span.start >= safeCut) null
            else {
                val clippedEnd = minOf(span.end, safeCut)
                if (span.start < clippedEnd) TextSpan(span.start, clippedEnd, span.type) else null
            }
        }
        val annotated = buildAnnotatedString {
            append(buildMonospaceAnnotatedString(baseText, visibleSpans, monoColor = monoColor, monoBackground = monoBg))
            append("… ")
            pushStringAnnotation(tag = "action", annotation = "more")
            pushStringAnnotation(tag = "more", annotation = "more")
            withStyle(SpanStyle(color = accentColor, fontSize = 15.sp)) {
                append("more")
            }
            pop()
        }
        return Pair(annotated, visibleSpans)
    }

    fun fitsInMaxLines(cut: Int): Boolean {
        val (candidate, _) = buildCandidate(cut)
        val measured = textMeasurer.measure(
            text = candidate,
            style = textStyle,
            constraints = constraints
        )
        return measured.lineCount <= maxLines
    }

    var bestCut = 0
    if (fitsInMaxLines(rawText.length)) {
        bestCut = rawText.length
    } else {
        var low = 0
        var high = rawText.length
        while (low <= high) {
            val mid = (low + high) / 2
            val adjustedMid = if (mid > 0 && mid < rawText.length &&
                rawText[mid - 1].isHighSurrogate() && rawText[mid].isLowSurrogate()
            ) {
                mid - 1
            } else {
                mid
            }

            if (fitsInMaxLines(adjustedMid)) {
                bestCut = adjustedMid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
    }

    val (finalText, visibleSpans) = buildCandidate(bestCut)
    return ListRowData(
        rawText = rawText.substring(0, bestCut.coerceIn(0, rawText.length)),
        annotatedText = finalText,
        spans = visibleSpans
    )
}

