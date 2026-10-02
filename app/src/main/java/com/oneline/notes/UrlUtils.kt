package com.oneline.notes

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.util.PatternsCompat

data class LinkSpan(
    val start: Int,
    val end: Int,
    val url: String
)

private val TRAILING_PUNCTUATION = charArrayOf('.', ',', ';', ':', '!', '?', ')')

/**
 * Detects http://, https://, www.-prefixed addresses, and bare domains with a real TLD
 * using Android's PatternsCompat.WEB_URL. Strips trailing punctuation (. , ; : ! ? )) from matches.
 */
fun extractUrlSpans(text: String): List<LinkSpan> {
    if (text.isEmpty()) return emptyList()
    val matcher = PatternsCompat.WEB_URL.matcher(text)
    val results = mutableListOf<LinkSpan>()

    while (matcher.find()) {
        val start = matcher.start()
        var end = matcher.end()
        while (end > start && text[end - 1] in TRAILING_PUNCTUATION) {
            end--
        }
        if (end > start) {
            val url = text.substring(start, end)
            results.add(LinkSpan(start, end, url))
        }
    }
    return results
}

/**
 * Normalizes URL with https:// scheme if no scheme is present.
 */
fun normalizeUrlForIntent(url: String): String {
    return if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
        "https://$url"
    } else {
        url
    }
}

/**
 * Builds AnnotatedString supporting both monospace formatting and auto-linked URLs.
 */
fun buildNoteBubbleAnnotatedString(
    text: String,
    monoSpans: List<TextSpan>,
    monoColor: Color,
    monoBackground: Color,
    linkAccentColor: Color
): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")
    val validMonoSpans = normalizeSpans(text.length, monoSpans)
    val urlSpans = extractUrlSpans(text)

    return buildAnnotatedString {
        append(text)

        // Monospace spans
        for (span in validMonoSpans) {
            val spanText = text.substring(span.start, span.end)
            addStringAnnotation(
                tag = "MONO",
                annotation = spanText,
                start = span.start,
                end = span.end
            )
            addStyle(
                style = SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = monoColor,
                    background = monoBackground
                ),
                start = span.start,
                end = span.end
            )
        }

        // URL spans: Underline + accent color #004296
        for (link in urlSpans) {
            addStringAnnotation(
                tag = "URL",
                annotation = link.url,
                start = link.start,
                end = link.end
            )
            addStyle(
                style = SpanStyle(
                    color = linkAccentColor,
                    textDecoration = TextDecoration.Underline
                ),
                start = link.start,
                end = link.end
            )
        }
    }
}

data class PartialCopyWord(
    val displayText: String,
    val value: String
)

/**
 * Tokenizes text for Copy Partially.
 * A detected link renders as one chip truncated to ~25 chars + "…",
 * but its selectable value and the copied text are always the full URL.
 */
fun tokenizeForPartialCopy(text: String): List<PartialCopyWord> {
    if (text.isBlank()) return emptyList()
    val urlSpans = extractUrlSpans(text)
    if (urlSpans.isEmpty()) {
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }.map {
            PartialCopyWord(displayText = it, value = it)
        }
    }

    val result = mutableListOf<PartialCopyWord>()
    var cursor = 0
    for (link in urlSpans) {
        if (link.start > cursor) {
            val nonUrlSegment = text.substring(cursor, link.start)
            val normalWords = nonUrlSegment.split(Regex("\\s+")).filter { it.isNotBlank() }
            for (w in normalWords) {
                result.add(PartialCopyWord(displayText = w, value = w))
            }
        }
        val fullUrl = link.url
        val displayUrl = if (fullUrl.length > 25) fullUrl.take(25) + "…" else fullUrl
        result.add(PartialCopyWord(displayText = displayUrl, value = fullUrl))
        cursor = link.end
    }
    if (cursor < text.length) {
        val nonUrlSegment = text.substring(cursor)
        val normalWords = nonUrlSegment.split(Regex("\\s+")).filter { it.isNotBlank() }
        for (w in normalWords) {
            result.add(PartialCopyWord(displayText = w, value = w))
        }
    }
    return result
}

/**
 * Launches ACTION_VIEW for URL. Invokes onNoHandler if no app can handle it or if an exception occurs.
 */
fun launchUrl(context: Context, url: String, onNoHandler: () -> Unit) {
    try {
        val uri = Uri.parse(normalizeUrlForIntent(url))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        onNoHandler()
    }
}
