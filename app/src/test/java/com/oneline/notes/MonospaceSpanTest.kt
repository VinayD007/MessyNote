package com.oneline.notes

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class MonospaceSpanTest {

    /**
     * Requirement: A single monospace span
     */
    @Test
    fun testSingleMonospaceSpan() {
        val text = "ABC123"
        val spans = listOf(TextSpan(start = 0, end = 6, type = "mono"))
        val normalized = normalizeSpans(text.length, spans)

        assertEquals(1, normalized.size)
        assertEquals(0, normalized[0].start)
        assertEquals(6, normalized[0].end)
        assertEquals("mono", normalized[0].type)

        // Verify hit detection at start, middle, and end within the span
        val hitStart = findMonospaceSpanAtOffset(text, normalized, 0)
        assertNotNull(hitStart)
        assertEquals("ABC123", text.substring(hitStart!!.start, hitStart.end))

        val hitMid = findMonospaceSpanAtOffset(text, normalized, 3)
        assertNotNull(hitMid)
        assertEquals("ABC123", text.substring(hitMid!!.start, hitMid.end))

        val hitEnd = findMonospaceSpanAtOffset(text, normalized, 5)
        assertNotNull(hitEnd)
        assertEquals("ABC123", text.substring(hitEnd!!.start, hitEnd.end))
    }

    /**
     * Requirement: Multiple monospace spans
     * Example: "Server: PROD-01, Port: 8080, User: admin"
     */
    @Test
    fun testMultipleMonospaceSpans() {
        val text = "Server: PROD-01, Port: 8080, User: admin"
        // "PROD-01" is at indices 8..15
        val prodStart = text.indexOf("PROD-01")
        val prodEnd = prodStart + "PROD-01".length
        // "8080" is at indices 23..27
        val portStart = text.indexOf("8080")
        val portEnd = portStart + "8080".length

        val spans = listOf(
            TextSpan(start = prodStart, end = prodEnd, type = "mono"),
            TextSpan(start = portStart, end = portEnd, type = "mono")
        )
        val normalized = normalizeSpans(text.length, spans)
        assertEquals(2, normalized.size)

        // Tap inside first span: copies only PROD-01
        val hitProd = findMonospaceSpanAtOffset(text, normalized, prodStart + 2)
        assertNotNull(hitProd)
        val prodText = text.substring(hitProd!!.start, hitProd.end)
        assertEquals("PROD-01", prodText)

        // Tap inside second span: copies only 8080
        val hitPort = findMonospaceSpanAtOffset(text, normalized, portStart + 1)
        assertNotNull(hitPort)
        val portText = text.substring(hitPort!!.start, hitPort.end)
        assertEquals("8080", portText)

        // Tap outside any span (in between)
        val betweenOffset = text.indexOf(", Port: ")
        val hitBetween = findMonospaceSpanAtOffset(text, normalized, betweenOffset)
        assertNull(hitBetween)
    }

    /**
     * Requirement: Monospace text in the middle of a sentence
     * Example: "Use this code: ABC123 to continue"
     */
    @Test
    fun testMonospaceInMiddleOfSentence() {
        val text = "Use this code: ABC123 to continue"
        val code = "ABC123"
        val start = text.indexOf(code)
        val end = start + code.length

        val spans = listOf(TextSpan(start = start, end = end, type = "mono"))
        val normalized = normalizeSpans(text.length, spans)

        assertEquals(1, normalized.size)
        assertEquals(start, normalized[0].start)
        assertEquals(end, normalized[0].end)

        // Tapping the middle monospace span
        val hitSpan = findMonospaceSpanAtOffset(text, normalized, start + 1)
        assertNotNull(hitSpan)
        assertEquals("ABC123", text.substring(hitSpan!!.start, hitSpan.end))

        // Tapping before the span: "Use this code:"
        val hitPrefix = findMonospaceSpanAtOffset(text, normalized, 2)
        assertNull("Tapping prefix text must return null", hitPrefix)

        // Tapping after the span: " to continue"
        val hitSuffix = findMonospaceSpanAtOffset(text, normalized, end + 3)
        assertNull("Tapping suffix text must return null", hitSuffix)
    }

    /**
     * Requirement: Editing/reopening a note while preserving monospace spans
     * (JSON serialization and deserialization lifecycle)
     */
    @Test
    fun testEditingAndReopeningNotePreservesSpans() {
        val originalText = "Token: SECRET-999-XYZ for service"
        val secret = "SECRET-999-XYZ"
        val start = originalText.indexOf(secret)
        val end = start + secret.length

        val originalSpans = listOf(TextSpan(start = start, end = end, type = "mono"))
        val note = NoteItem(
            id = "test-uuid-1",
            text = originalText,
            createdAt = 1700000000000L,
            updatedAt = 1700000500000L,
            spans = originalSpans.toMutableList()
        )

        // Simulate save to JSON
        val obj = JSONObject().apply {
            put("id", note.id)
            put("text", note.text)
            put("createdAt", note.createdAt)
            put("updatedAt", note.updatedAt)
            put("spans", spansToJson(note.spans))
        }

        val jsonString = obj.toString()

        // Simulate reopen / load from JSON
        val loadedObj = JSONObject(jsonString)
        val loadedSpans = spansFromJson(loadedObj.optJSONArray("spans"))
        val restoredNote = NoteItem(
            id = loadedObj.getString("id"),
            text = loadedObj.getString("text"),
            createdAt = loadedObj.getLong("createdAt"),
            updatedAt = if (loadedObj.has("updatedAt")) loadedObj.getLong("updatedAt") else null,
            spans = loadedSpans.toMutableList()
        )

        assertEquals(note.id, restoredNote.id)
        assertEquals(note.text, restoredNote.text)
        assertEquals(note.createdAt, restoredNote.createdAt)
        assertEquals(note.updatedAt, restoredNote.updatedAt)
        assertEquals(1, restoredNote.spans.size)
        assertEquals(start, restoredNote.spans[0].start)
        assertEquals(end, restoredNote.spans[0].end)
        assertEquals("mono", restoredNote.spans[0].type)

        // Ensure restored span works for copying
        val hit = findMonospaceSpanAtOffset(restoredNote.text, restoredNote.spans, start + 3)
        assertNotNull(hit)
        assertEquals(secret, restoredNote.text.substring(hit!!.start, hit.end))
    }

    /**
     * Requirement: Tapping normal text does not copy it
     */
    @Test
    fun testTappingNormalTextDoesNotCopy() {
        val text = "Please contact support@example.com for help"
        val spans = listOf(TextSpan(start = 15, end = 34, type = "mono")) // support@example.com

        // Test normal text before span
        for (i in 0 until 15) {
            val hit = findMonospaceSpanAtOffset(text, spans, i)
            assertNull("Offset $i in normal text must not match any monospace span", hit)
        }

        // Test normal text after span
        for (i in 34 until text.length) {
            val hit = findMonospaceSpanAtOffset(text, spans, i)
            assertNull("Offset $i in normal text must not match any monospace span", hit)
        }

        // Out of bounds offsets
        assertNull(findMonospaceSpanAtOffset(text, spans, -1))
        assertNull(findMonospaceSpanAtOffset(text, spans, text.length))
        assertNull(findMonospaceSpanAtOffset(text, spans, 100))
    }

    /**
     * Requirement: Tapping a monospace span copies only that span
     * (exact underlying text, without extra quotation marks, backticks, or formatting characters)
     */
    @Test
    fun testTappingMonospaceSpanCopiesOnlyThatSpan() {
        val text = "Run command: git checkout main in terminal"
        val cmd = "git checkout main"
        val start = text.indexOf(cmd)
        val end = start + cmd.length

        val spans = listOf(TextSpan(start = start, end = end, type = "mono"))
        val hit = findMonospaceSpanAtOffset(text, spans, start + 5)

        assertNotNull(hit)
        val extractedText = text.substring(hit!!.start, hit.end)

        assertEquals("git checkout main", extractedText)
        assertFalse(extractedText.startsWith("`"))
        assertFalse(extractedText.endsWith("`"))
        assertFalse(extractedText.startsWith("\""))
        assertFalse(extractedText.endsWith("\""))
    }

    /**
     * Span maintenance when editing text
     */
    @Test
    fun testSpanAdjustmentOnTextEdit() {
        val originalText = "Key: 123456"
        val originalSpans = listOf(TextSpan(start = 5, end = 11, type = "mono")) // "123456"

        // Insert text before the span: "My Key: 123456" (added 3 characters at index 0)
        val newTextPrefix = "My Key: 123456"
        val adjustedPrefix = adjustSpansForTextChange(originalText, newTextPrefix, originalSpans)
        assertEquals(1, adjustedPrefix.size)
        assertEquals(8, adjustedPrefix[0].start)
        assertEquals(14, adjustedPrefix[0].end)
        assertEquals("123456", newTextPrefix.substring(adjustedPrefix[0].start, adjustedPrefix[0].end))

        // Append text after the span: "Key: 123456 is secret"
        val newTextSuffix = "Key: 123456 is secret"
        val adjustedSuffix = adjustSpansForTextChange(originalText, newTextSuffix, originalSpans)
        assertEquals(1, adjustedSuffix.size)
        assertEquals(5, adjustedSuffix[0].start)
        assertEquals(11, adjustedSuffix[0].end)
        assertEquals("123456", newTextSuffix.substring(adjustedSuffix[0].start, adjustedSuffix[0].end))
    }

    /**
     * Markdown backticks parser test
     */
    @Test
    fun testMarkdownBackticksParsing() {
        val raw = "Use `ABC123` or `XYZ789` code"
        val (cleanText, spans) = parseMarkdownBackticks(raw)

        assertEquals("Use ABC123 or XYZ789 code", cleanText)
        assertEquals(2, spans.size)

        assertEquals("ABC123", cleanText.substring(spans[0].start, spans[0].end))
        assertEquals("XYZ789", cleanText.substring(spans[1].start, spans[1].end))
    }

    /**
     * Monospace span toggle functionality test
     */
    @Test
    fun testToggleMonospaceSpan() {
        val text = "Hello World"
        val spans = emptyList<TextSpan>()

        // Apply Mono to "World" (index 6..11)
        val withMono = toggleMonospaceSpan(text.length, spans, 6, 11)
        assertEquals(1, withMono.size)
        assertEquals(6, withMono[0].start)
        assertEquals(11, withMono[0].end)

        // Toggle Mono again on the exact same selection -> removes it
        val withoutMono = toggleMonospaceSpan(text.length, withMono, 6, 11)
        assertTrue(withoutMono.isEmpty())
    }

    /**
     * Requirement: Edit an existing note, verify state updates in list and preserves spans
     */
    @Test
    fun testNoteEditingUpdatesInList() {
        val note1 = NoteItem(id = "1", text = "Original note one")
        val note2 = NoteItem(
            id = "2",
            text = "Use code ABC123 now",
            spans = mutableListOf(TextSpan(start = 9, end = 15, type = "mono")) // "ABC123"
        )
        val noteList = mutableListOf(note1, note2)

        // 1. Edit note 2: change text to "Use code ABC999 now", adjust span
        val target = noteList[1]
        val newText = "Use code ABC999 now"
        val updatedSpans = adjustSpansForTextChange(target.text, newText, target.spans)
        val updatedNote2 = target.copy(
            text = newText,
            spans = updatedSpans.toMutableList(),
            updatedAt = System.currentTimeMillis()
        )

        val index = noteList.indexOfFirst { it.id == target.id }
        assertEquals(1, index)
        noteList[index] = updatedNote2

        // Verify note 2 is immediately updated in the list
        assertEquals("Use code ABC999 now", noteList[1].text)
        assertNotNull(noteList[1].updatedAt)
        assertEquals(1, noteList[1].spans.size)
        assertEquals("ABC999", noteList[1].text.substring(noteList[1].spans[0].start, noteList[1].spans[0].end))

        // Verify note 1 remains completely unchanged
        assertEquals("Original note one", noteList[0].text)
        assertNull(noteList[0].updatedAt)

        // 2. Edit note 2 a second time: replace "ABC999" with "XYZ-000"
        val secondNewText = "Use code XYZ-000 now"
        val secondSpans = adjustSpansForTextChange(updatedNote2.text, secondNewText, updatedNote2.spans)
        val secondUpdatedNote2 = updatedNote2.copy(
            text = secondNewText,
            spans = secondSpans.toMutableList(),
            updatedAt = System.currentTimeMillis() + 1000
        )
        noteList[1] = secondUpdatedNote2

        assertEquals("Use code XYZ-000 now", noteList[1].text)
        assertEquals(1, noteList[1].spans.size)
        assertEquals("XYZ-000", noteList[1].text.substring(noteList[1].spans[0].start, noteList[1].spans[0].end))

        // Tap-to-copy hit detection on updated note
        val hit = findMonospaceSpanAtOffset(noteList[1].text, noteList[1].spans, 9)
        assertNotNull(hit)
        assertEquals("XYZ-000", noteList[1].text.substring(hit!!.start, hit.end))
    }

    @Test
    fun testStructuredNoteCreationAndProperties() {
        val structuredNote = NoteItem(
            text = "Phone = 9876543210",
            type = NoteType.FIELD_VALUE,
            field = "Phone",
            value = "9876543210"
        )

        assertEquals(NoteType.FIELD_VALUE, structuredNote.type)
        assertEquals("Phone", structuredNote.field)
        assertEquals("9876543210", structuredNote.value)
        assertEquals("Phone = 9876543210", structuredNote.text)

        // Normal note defaults
        val normalNote = NoteItem(text = "Hello world")
        assertEquals(NoteType.NORMAL, normalNote.type)
        assertNull(normalNote.field)
        assertNull(normalNote.value)
    }

    @Test
    fun testStructuredNoteSerializationAndDeserialization() {
        val note = NoteItem(
            id = "field-note-1",
            text = "Email = john@example.com",
            type = NoteType.FIELD_VALUE,
            field = "Email",
            value = "john@example.com",
            createdAt = 1700000000000L
        )

        // Serialize
        val obj = JSONObject().apply {
            put("id", note.id)
            put("text", note.text)
            put("createdAt", note.createdAt)
            if (note.updatedAt != null) put("updatedAt", note.updatedAt)
            if (note.spans.isNotEmpty()) put("spans", spansToJson(note.spans))
            put("type", note.type.name)
            if (note.field != null) put("field", note.field)
            if (note.value != null) put("value", note.value)
        }

        val jsonStr = obj.toString()

        // Deserialize
        val loadedObj = JSONObject(jsonStr)
        val typeStr = loadedObj.optString("type", NoteType.NORMAL.name)
        val type = try { NoteType.valueOf(typeStr) } catch (e: Exception) { NoteType.NORMAL }
        val restoredNote = NoteItem(
            id = loadedObj.getString("id"),
            text = loadedObj.getString("text"),
            createdAt = loadedObj.getLong("createdAt"),
            updatedAt = if (loadedObj.has("updatedAt")) loadedObj.getLong("updatedAt") else null,
            spans = spansFromJson(loadedObj.optJSONArray("spans")).toMutableList(),
            type = type,
            field = if (loadedObj.has("field")) loadedObj.getString("field") else null,
            value = if (loadedObj.has("value")) loadedObj.getString("value") else null
        )

        assertEquals(note.id, restoredNote.id)
        assertEquals(NoteType.FIELD_VALUE, restoredNote.type)
        assertEquals("Email", restoredNote.field)
        assertEquals("john@example.com", restoredNote.value)
        assertEquals("Email = john@example.com", restoredNote.text)
    }

    @Test
    fun testLegacyNoteDeserializationBackwardsCompatibility() {
        // Legacy JSON without "type", "field", or "value"
        val legacyJson = """
            {
                "id": "legacy-id-1",
                "text": "Buy groceries",
                "createdAt": 1699999999999
            }
        """.trimIndent()

        val loadedObj = JSONObject(legacyJson)
        val typeStr = loadedObj.optString("type", NoteType.NORMAL.name)
        val type = try { NoteType.valueOf(typeStr) } catch (e: Exception) { NoteType.NORMAL }
        val restoredNote = NoteItem(
            id = loadedObj.getString("id"),
            text = loadedObj.getString("text"),
            createdAt = loadedObj.getLong("createdAt"),
            updatedAt = if (loadedObj.has("updatedAt")) loadedObj.getLong("updatedAt") else null,
            spans = spansFromJson(loadedObj.optJSONArray("spans")).toMutableList(),
            type = type,
            field = if (loadedObj.has("field")) loadedObj.getString("field") else null,
            value = if (loadedObj.has("value")) loadedObj.getString("value") else null
        )

        assertEquals("legacy-id-1", restoredNote.id)
        assertEquals("Buy groceries", restoredNote.text)
        assertEquals(NoteType.NORMAL, restoredNote.type)
        assertNull(restoredNote.field)
        assertNull(restoredNote.value)
    }

    @Test
    fun testStructuredNoteSearchMatching() {
        val notes = listOf(
            NoteItem(id = "1", text = "Buy milk", type = NoteType.NORMAL),
            NoteItem(id = "2", text = "Phone = 9876543210", type = NoteType.FIELD_VALUE, field = "Phone", value = "9876543210"),
            NoteItem(id = "3", text = "Name = John", type = NoteType.FIELD_VALUE, field = "Name", value = "John"),
            NoteItem(id = "4", text = "Employee ID = EMP-1024", type = NoteType.FIELD_VALUE, field = "Employee ID", value = "EMP-1024")
        )

        fun search(query: String): List<NoteItem> {
            val q = query.trim()
            return notes.filter { note ->
                if (note.type == NoteType.FIELD_VALUE) {
                    (note.field?.contains(q, ignoreCase = true) == true) ||
                    (note.value?.contains(q, ignoreCase = true) == true) ||
                    note.text.contains(q, ignoreCase = true)
                } else {
                    note.text.contains(q, ignoreCase = true)
                }
            }
        }

        // Search by Field
        val phoneResults = search("Phone")
        assertEquals(1, phoneResults.size)
        assertEquals("2", phoneResults[0].id)

        // Search by Value
        val numResults = search("9876543210")
        assertEquals(1, numResults.size)
        assertEquals("2", numResults[0].id)

        val johnResults = search("john")
        assertEquals(1, johnResults.size)
        assertEquals("3", johnResults[0].id)

        // Search by partial value with hyphen
        val empResults = search("EMP")
        assertEquals(1, empResults.size)
        assertEquals("4", empResults[0].id)

        // Normal note search
        val milkResults = search("milk")
        assertEquals(1, milkResults.size)
        assertEquals("1", milkResults[0].id)
    }
}
