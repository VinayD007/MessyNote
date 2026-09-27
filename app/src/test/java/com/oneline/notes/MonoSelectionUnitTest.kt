package com.oneline.notes

import org.junit.Assert.*
import org.junit.Test

class MonoSelectionUnitTest {

    /**
     * Requirement: Partial selection
     * "Meeting with PROD-123 tomorrow"
     * Select: "PROD-123"
     * Result: Only "PROD-123" is monospace.
     */
    @Test
    fun testPartialSelectionMonoFormatting() {
        val text = "Meeting with PROD-123 tomorrow"
        val start = text.indexOf("PROD-123")
        val end = start + "PROD-123".length
        assertTrue("PROD-123 must be present in text", start >= 0)

        val initialSpans = emptyList<TextSpan>()
        val resultSpans = toggleMonospaceSpan(text.length, initialSpans, start, end)

        assertEquals(1, resultSpans.size)
        assertEquals(start, resultSpans[0].start)
        assertEquals(end, resultSpans[0].end)
        assertEquals("mono", resultSpans[0].type)
        assertEquals("PROD-123", text.substring(resultSpans[0].start, resultSpans[0].end))

        // After sending, tap-to-copy returns only PROD-123
        val tapOffset = start + 2 // within "PROD-123"
        val tappedSpan = findMonospaceSpanAtOffset(text, resultSpans, tapOffset)
        assertNotNull(tappedSpan)
        assertEquals("PROD-123", text.substring(tappedSpan!!.start, tappedSpan.end))

        // Tapping outside PROD-123 does not trigger monospace copy
        val outsideOffset = text.indexOf("tomorrow")
        val outsideSpan = findMonospaceSpanAtOffset(text, resultSpans, outsideOffset)
        assertNull(outsideSpan)
    }

    /**
     * Requirement: Another partial selection example
     * "Use PROD-01 for testing"
     * Select: "PROD-01"
     * Result: Only "PROD-01" becomes monospace.
     */
    @Test
    fun testPartialSelectionProd01() {
        val text = "Use PROD-01 for testing"
        val start = text.indexOf("PROD-01")
        val end = start + "PROD-01".length

        val resultSpans = toggleMonospaceSpan(text.length, emptyList(), start, end)
        assertEquals(1, resultSpans.size)
        assertEquals("PROD-01", text.substring(resultSpans[0].start, resultSpans[0].end))
    }

    /**
     * Requirement: Full selection
     * "Use PROD-01 for testing"
     * Select entire message.
     * Result: The entire message becomes monospace.
     */
    @Test
    fun testFullSelectionMonoFormatting() {
        val text = "Use PROD-01 for testing"
        val start = 0
        val end = text.length

        val resultSpans = toggleMonospaceSpan(text.length, emptyList(), start, end)
        assertEquals(1, resultSpans.size)
        assertEquals(0, resultSpans[0].start)
        assertEquals(text.length, resultSpans[0].end)
        assertEquals(text, text.substring(resultSpans[0].start, resultSpans[0].end))

        // Tap inside anywhere copies the entire message
        val tapOffset = 5
        val tappedSpan = findMonospaceSpanAtOffset(text, resultSpans, tapOffset)
        assertNotNull(tappedSpan)
        assertEquals(text, text.substring(tappedSpan!!.start, tappedSpan.end))
    }

    /**
     * Requirement: Empty or collapsed selection
     * If no text is selected:
     * - Mono should not apply formatting.
     * - Do not crash.
     * - Do not alter the message.
     */
    @Test
    fun testEmptySelectionDoesNotAlterSpans() {
        val text = "Some test message"
        val initialSpans = emptyList<TextSpan>()

        // Cursor placed at index 5 without selection (start == end)
        val resultSpans = toggleMonospaceSpan(text.length, initialSpans, 5, 5)
        assertTrue(resultSpans.isEmpty())

        // Inverted range
        val invertedSpans = toggleMonospaceSpan(text.length, initialSpans, 10, 5)
        assertTrue(invertedSpans.isEmpty())
    }

    /**
     * Requirement: Preserving formatting when editing text
     */
    @Test
    fun testSpansPreservedOnTextEdit() {
        val originalText = "Hello PROD-123 world"
        val start = originalText.indexOf("PROD-123")
        val end = start + "PROD-123".length
        val originalSpans = toggleMonospaceSpan(originalText.length, emptyList(), start, end)

        // Typing prefix at beginning
        val newText = "Quick Hello PROD-123 world"
        val adjustedSpans = adjustSpansForTextChange(originalText, newText, originalSpans)

        assertEquals(1, adjustedSpans.size)
        assertEquals("PROD-123", newText.substring(adjustedSpans[0].start, adjustedSpans[0].end))
    }

    /**
     * Requirement: Trimming leading/trailing whitespace when sending note
     */
    @Test
    fun testTrimmingOnSendPreservesMonospaceSpan() {
        val rawText = "   Meeting with PROD-123 tomorrow   "
        val start = rawText.indexOf("PROD-123")
        val end = start + "PROD-123".length
        val composerSpans = toggleMonospaceSpan(rawText.length, emptyList(), start, end)

        val trimmed = rawText.trim()
        val adjusted = adjustSpansForTextChange(rawText, trimmed, composerSpans)

        assertEquals(1, adjusted.size)
        assertEquals("PROD-123", trimmed.substring(adjusted[0].start, adjusted[0].end))
    }
}
