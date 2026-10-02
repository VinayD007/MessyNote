package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlDetectionTest {

    @Test
    fun testDetectVariousUrlFormats() {
        val text = "Visit https://example.com/path, and www.test.org:8080/file, also bare domain google.com."
        val spans = extractUrlSpans(text)
        assertEquals(3, spans.size)

        assertEquals("https://example.com/path", spans[0].url)
        assertEquals("www.test.org:8080/file", spans[1].url)
        assertEquals("google.com", spans[2].url)
    }

    @Test
    fun testStripAllSpecifiedTrailingPunctuation() {
        // (. , ; : ! ? ))
        val text = "A: https://site.com. B: https://site.com, C: https://site.com; D: https://site.com: E: https://site.com! F: https://site.com? G: (https://site.com)"
        val spans = extractUrlSpans(text)
        assertEquals(7, spans.size)

        for (span in spans) {
            assertEquals("https://site.com", span.url)
        }
    }

    @Test
    fun testPartialCopyTokenization() {
        val longUrl = "https://www.google.com/search?q=very_long_search_query_here"
        val text = "Check out $longUrl today."
        val chips = tokenizeForPartialCopy(text)

        assertEquals(4, chips.size)
        assertEquals("Check", chips[0].displayText)
        assertEquals("Check", chips[0].value)

        assertEquals("out", chips[1].displayText)
        assertEquals("out", chips[1].value)

        // URL chip: truncated to ~25 chars + "…"
        assertTrue(chips[2].displayText.length <= 26) // 25 + 1 char for ellipsis
        assertEquals(longUrl.take(25) + "…", chips[2].displayText)
        assertEquals(longUrl, chips[2].value)

        assertEquals("today.", chips[3].displayText)
        assertEquals("today.", chips[3].value)
    }

    @Test
    fun testPartialCopyShortUrlNotTruncated() {
        val shortUrl = "https://short.io"
        val text = "Link: $shortUrl"
        val chips = tokenizeForPartialCopy(text)

        assertEquals(2, chips.size)
        assertEquals("Link:", chips[0].displayText)
        assertEquals(shortUrl, chips[1].displayText)
        assertEquals(shortUrl, chips[1].value)
    }

    @Test
    fun testNormalizeUrlForIntent() {
        assertEquals("https://google.com", normalizeUrlForIntent("google.com"))
        assertEquals("https://www.google.com", normalizeUrlForIntent("www.google.com"))
        assertEquals("http://insecure.org", normalizeUrlForIntent("http://insecure.org"))
        assertEquals("https://secure.org", normalizeUrlForIntent("https://secure.org"))
    }
}
