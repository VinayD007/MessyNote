package com.oneline.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.json.JSONArray
import org.json.JSONObject

/**
 * Structured range representing formatted text within a note.
 */
data class TextSpan(
    val start: Int,
    val end: Int,
    val type: String = "mono"
)

/**
 * Normalizes, bounds-checks, and merges contiguous/overlapping spans of the same type.
 */
fun normalizeSpans(textLength: Int, spans: List<TextSpan>): List<TextSpan> {
    if (textLength <= 0) return emptyList()
    val valid = spans.mapNotNull { span ->
        val s = span.start.coerceIn(0, textLength)
        val e = span.end.coerceIn(0, textLength)
        if (s < e) TextSpan(s, e, span.type) else null
    }.sortedBy { it.start }

    if (valid.isEmpty()) return emptyList()

    val merged = mutableListOf<TextSpan>()
    var current = valid[0]
    for (i in 1 until valid.size) {
        val next = valid[i]
        if (next.start <= current.end && next.type == current.type) {
            current = TextSpan(current.start, maxOf(current.end, next.end), current.type)
        } else {
            merged.add(current)
            current = next
        }
    }
    merged.add(current)
    return merged
}

/**
 * Toggles a monospace span over [start, end].
 * If the range is already fully monospace, removes/splits it.
 * Otherwise, adds monospace formatting to that range.
 */
fun toggleMonospaceSpan(textLength: Int, spans: List<TextSpan>, start: Int, end: Int): List<TextSpan> {
    if (start >= end || textLength <= 0) return spans
    val s = start.coerceIn(0, textLength)
    val e = end.coerceIn(0, textLength)
    if (s >= e) return spans

    val isAlreadyMono = spans.any { it.type == "mono" && it.start <= s && it.end >= e }
    return if (isAlreadyMono) {
        val result = mutableListOf<TextSpan>()
        for (span in spans) {
            if (span.type == "mono" && span.start < e && span.end > s) {
                if (span.start < s) {
                    result.add(TextSpan(span.start, s, span.type))
                }
                if (span.end > e) {
                    result.add(TextSpan(e, span.end, span.type))
                }
            } else {
                result.add(span)
            }
        }
        normalizeSpans(textLength, result)
    } else {
        val newSpans = spans.toMutableList()
        newSpans.add(TextSpan(s, e, "mono"))
        normalizeSpans(textLength, newSpans)
    }
}

/**
 * Adjusts span indices specifically when a string is trimmed of leading and/or trailing whitespace.
 */
fun adjustSpansForTrim(oldText: String, trimmedText: String, spans: List<TextSpan>): List<TextSpan> {
    if (trimmedText.isEmpty() || spans.isEmpty()) return emptyList()
    val leading = oldText.takeWhile { it.isWhitespace() }.length
    val shifted = spans.mapNotNull { span ->
        val s = (span.start - leading).coerceIn(0, trimmedText.length)
        val e = (span.end - leading).coerceIn(0, trimmedText.length)
        if (s < e) TextSpan(s, e, span.type) else null
    }
    return normalizeSpans(trimmedText.length, shifted)
}

/**
 * Adjusts span indices when text is edited (inserted, replaced, or deleted).
 */
fun adjustSpansForTextChange(oldText: String, newText: String, oldSpans: List<TextSpan>): List<TextSpan> {
    if (newText.isEmpty()) return emptyList()
    if (oldSpans.isEmpty()) return emptyList()
    if (oldText == newText) return normalizeSpans(newText.length, oldSpans)
    if (newText == oldText.trim()) return adjustSpansForTrim(oldText, newText, oldSpans)

    var prefixLen = 0
    val minLen = minOf(oldText.length, newText.length)
    while (prefixLen < minLen && oldText[prefixLen] == newText[prefixLen]) {
        prefixLen++
    }

    var oldSuffixLen = 0
    var newSuffixLen = 0
    while (oldSuffixLen < (oldText.length - prefixLen) &&
        newSuffixLen < (newText.length - prefixLen) &&
        oldText[oldText.length - 1 - oldSuffixLen] == newText[newText.length - 1 - newSuffixLen]
    ) {
        oldSuffixLen++
        newSuffixLen++
    }

    val oldEditStart = prefixLen
    val oldEditEnd = oldText.length - oldSuffixLen
    val newEditEnd = newText.length - newSuffixLen
    val delta = (newEditEnd - oldEditStart) - (oldEditEnd - oldEditStart)

    val updatedSpans = mutableListOf<TextSpan>()
    for (span in oldSpans) {
        when {
            // Span is entirely before the edit point
            span.end <= oldEditStart -> {
                updatedSpans.add(span)
            }
            // Span is entirely after the edit point
            span.start >= oldEditEnd -> {
                val newStart = span.start + delta
                val newEnd = span.end + delta
                if (newStart in 0..newText.length && newEnd in 0..newText.length && newStart < newEnd) {
                    updatedSpans.add(TextSpan(newStart, newEnd, span.type))
                }
            }
            // Span overlaps the edit region
            else -> {
                val newStart = if (span.start < oldEditStart) span.start else oldEditStart
                val newEnd = if (span.end > oldEditEnd) span.end + delta else newEditEnd
                if (newStart < newEnd) {
                    updatedSpans.add(TextSpan(newStart.coerceAtLeast(0), newEnd.coerceAtMost(newText.length), span.type))
                }
            }
        }
    }
    return normalizeSpans(newText.length, updatedSpans)
}

/**
 * Finds the monospace span containing the given character offset, or null if offset is outside monospace spans.
 */
fun findMonospaceSpanAtOffset(text: String, spans: List<TextSpan>, offset: Int): TextSpan? {
    if (offset < 0 || offset >= text.length) return null
    return spans.firstOrNull { it.type == "mono" && offset >= it.start && offset < it.end }
}

/**
 * Parses markdown backticks e.g. `ABC123` into clean text and structured TextSpans.
 */
fun parseMarkdownBackticks(rawText: String): Pair<String, List<TextSpan>> {
    val regex = Regex("`([^`]+)`")
    val spans = mutableListOf<TextSpan>()
    val sb = StringBuilder()
    var lastIndex = 0

    for (match in regex.findAll(rawText)) {
        sb.append(rawText.substring(lastIndex, match.range.first))
        val start = sb.length
        val content = match.groupValues[1]
        sb.append(content)
        val end = sb.length
        spans.add(TextSpan(start, end, "mono"))
        lastIndex = match.range.last + 1
    }
    if (lastIndex < rawText.length) {
        sb.append(rawText.substring(lastIndex))
    }
    return Pair(sb.toString(), spans)
}

/**
 * Builds an AnnotatedString with distinct monospace visual styling and string annotations.
 */
fun buildMonospaceAnnotatedString(
    text: String,
    spans: List<TextSpan>,
    monoColor: Color,
    monoBackground: Color
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val validSpans = normalizeSpans(text.length, spans)
    return buildAnnotatedString {
        var cursor = 0
        for (span in validSpans) {
            if (span.start > cursor) {
                append(text.substring(cursor, span.start))
            }
            val spanText = text.substring(span.start, span.end)
            pushStringAnnotation(tag = "MONO", annotation = spanText)
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = monoColor,
                    background = monoBackground
                )
            ) {
                append(spanText)
            }
            pop()
            cursor = span.end
        }
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}

/**
 * Serialization helper for spans to JSON.
 */
fun spansToJson(spans: List<TextSpan>): JSONArray {
    val array = JSONArray()
    for (s in spans) {
        val obj = JSONObject().apply {
            put("start", s.start)
            put("end", s.end)
            put("type", s.type)
        }
        array.put(obj)
    }
    return array
}

/**
 * Deserialization helper for spans from JSON.
 */
fun spansFromJson(array: JSONArray?): List<TextSpan> {
    if (array == null) return emptyList()
    val list = mutableListOf<TextSpan>()
    for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
            TextSpan(
                start = obj.getInt("start"),
                end = obj.getInt("end"),
                type = obj.optString("type", "mono")
            )
        )
    }
    return list
}
