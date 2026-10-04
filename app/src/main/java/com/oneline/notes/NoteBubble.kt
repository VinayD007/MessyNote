package com.oneline.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.oneline.notes.ui.theme.*

@Suppress("DEPRECATION")
@Composable
fun SentNoteBubble(
    note: NoteItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMonoTap: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false
) {
    val context = LocalContext.current
    val sendTransientMessage = LocalTransientMessageSender.current
    val linkAccentColor = LinkAccent
    var linkToOpen by remember { mutableStateOf<String?>(null) }

    val handleTextTap: (offset: Int, text: String, monoSpans: List<TextSpan>, urlSpans: List<LinkSpan>) -> Unit = { offset, fullText, monoSpans, urlSpans ->
        if (isSelectionMode) {
            onClick()
        } else {
            val clickedMono = monoSpans.firstOrNull { offset in it.start until it.end }
            if (clickedMono != null) {
                val spanText = fullText.substring(
                    clickedMono.start.coerceIn(0, fullText.length),
                    clickedMono.end.coerceIn(0, fullText.length)
                )
                onMonoTap(spanText)
            } else {
                val clickedLink = urlSpans.firstOrNull { offset in it.start until it.end }
                if (clickedLink != null) {
                    linkToOpen = clickedLink.url
                } else {
                    onClick()
                }
            }
        }
    }

    val currentMonoText = MonospaceText
    val currentMonoBg = MonospaceBg
    val normalUrlSpans = remember(note.text) { extractUrlSpans(note.text) }
    val annotatedText = remember(note.text, note.spans.toList(), currentMonoText, currentMonoBg, linkAccentColor, normalUrlSpans) {
        buildNoteBubbleAnnotatedString(
            text = note.text,
            monoSpans = note.spans,
            monoColor = currentMonoText,
            monoBackground = currentMonoBg,
            linkAccentColor = linkAccentColor
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, NoteBubbleAccent),
        shadowElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                )
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

                    val density = LocalDensity.current
                    val textMeasurer = rememberTextMeasurer()
                    val fieldStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start
                    )
                    val numberStyle = TextStyle(
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val fieldColumns = remember(sortedItems, maxWidth, density, textMeasurer) {
                            val maxMeasureWidthPx = with(density) { maxWidth.roundToPx() }
                            val numberWidthPx = textMeasurer.measure(
                                AnnotatedString("${sortedItems.size}. "),
                                style = numberStyle,
                                constraints = androidx.compose.ui.unit.Constraints(maxWidth = maxMeasureWidthPx)
                            ).size.width
                            val maxFieldWidthPx = sortedItems.maxOfOrNull { item ->
                                textMeasurer.measure(
                                    AnnotatedString(item.field),
                                    style = fieldStyle,
                                    constraints = Constraints(maxWidth = maxMeasureWidthPx)
                                ).size.width
                            } ?: 0
                            val fieldWidthLimit = with(density) {
                                minOf(
                                    (maxWidth * 0.42f).roundToPx(),
                                    (maxWidth - numberWidthPx.toDp() - 68.dp).coerceAtLeast(1.dp).roundToPx()
                                )
                            }
                            with(density) {
                                numberWidthPx.toDp() to
                                    maxFieldWidthPx.coerceAtMost(fieldWidthLimit).coerceAtLeast(1).toDp()
                            }
                        }
                        val numberColumn = fieldColumns.first
                        val separatorColumn = fieldColumns.second

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sortedItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${index + 1}. ",
                                        style = numberStyle,
                                        modifier = Modifier.width(numberColumn)
                                    )
                                    val fieldUrlSpans = remember(item.field) { extractUrlSpans(item.field) }
                                    val fieldAnnotated = remember(item.field, linkAccentColor, fieldUrlSpans) {
                                        buildNoteBubbleAnnotatedString(
                                            text = item.field,
                                            monoSpans = emptyList(),
                                            monoColor = currentMonoText,
                                            monoBackground = currentMonoBg,
                                            linkAccentColor = linkAccentColor
                                        )
                                    }
                                    ClickableText(
                                        text = fieldAnnotated,
                                        modifier = Modifier.width(separatorColumn),
                                        style = fieldStyle,
                                        onClick = { offset ->
                                            handleTextTap(offset, item.field, emptyList(), fieldUrlSpans)
                                        }
                                    )
                                    Text(
                                        text = "=",
                                        color = TextSecondary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    if (item.value.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = currentMonoBg,
                                            modifier = Modifier
                                                .weight(1f, fill = false)
                                                .clickable {
                                                    if (isSelectionMode) {
                                                        onClick()
                                                    } else {
                                                        onMonoTap(item.value)
                                                    }
                                                }
                                        ) {
                                            val valueUrlSpans = remember(item.value) { extractUrlSpans(item.value) }
                                            val valueAnnotated = remember(item.value, valueUrlSpans, currentMonoText, currentMonoBg, linkAccentColor) {
                                                buildNoteBubbleAnnotatedString(
                                                    text = item.value,
                                                    monoSpans = emptyList(),
                                                    monoColor = currentMonoText,
                                                    monoBackground = currentMonoBg,
                                                    linkAccentColor = linkAccentColor
                                                )
                                            }
                                            Text(
                                                text = valueAnnotated,
                                                fontFamily = FontFamily.Monospace,
                                                color = currentMonoText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                textAlign = TextAlign.Start,
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

                    val rowAnnotatedStrings = remember(rawRows, note.spans.toList(), currentMonoText, currentMonoBg, linkAccentColor) {
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
                            val rowUrlSpans = extractUrlSpans(rowStr)
                            ListRowData(
                                rawText = rowStr,
                                annotatedText = buildNoteBubbleAnnotatedString(
                                    text = rowStr,
                                    monoSpans = rowSpans,
                                    monoColor = currentMonoText,
                                    monoBackground = currentMonoBg,
                                    linkAccentColor = linkAccentColor
                                ),
                                spans = rowSpans,
                                urlSpans = rowUrlSpans
                            )
                        }
                    }

                    val textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowAnnotatedStrings.forEach { rowData ->
                            ClickableText(
                                text = rowData.annotatedText,
                                style = textStyle,
                                onClick = { offset ->
                                    handleTextTap(offset, rowData.rawText, rowData.spans, rowData.urlSpans)
                                }
                            )
                        }
                    }
                } else {
                    val textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )

                    ClickableText(
                        text = annotatedText,
                        modifier = Modifier,
                        onClick = { offset ->
                            handleTextTap(offset, note.text, note.spans, normalUrlSpans)
                        },
                        style = textStyle
                    )
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

    if (linkToOpen != null) {
        val url = linkToOpen!!
        AlertDialog(
            onDismissRequest = { linkToOpen = null },
            containerColor = SurfaceDark,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Open link?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = url,
                    color = TextSecondary,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        linkToOpen = null
                        launchUrl(context, url) {
                            sendTransientMessage("No application found to open link")
                        }
                    }
                ) {
                    Text(
                        text = "Open",
                        color = BrightBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { linkToOpen = null }) {
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
}

private data class ListRowData(
    val rawText: String,
    val annotatedText: AnnotatedString,
    val spans: List<TextSpan>,
    val urlSpans: List<LinkSpan>
)
