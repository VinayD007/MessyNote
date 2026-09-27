package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteStorageTest {

    @Test
    fun testParseNotesFromJson_validCorruptValid_returnsTwoNotesAndErrorFlag() {
        val jsonInput = """
            [
                {"id":"1","text":"First valid note","createdAt":1000,"type":"NORMAL"},
                {"corrupt_entry": true},
                {"id":"2","text":"Second valid note","createdAt":2000,"type":"NORMAL"}
            ]
        """.trimIndent()

        val result = parseNotesFromJson(jsonInput)

        assertEquals(2, result.notes.size)
        assertEquals("First valid note", result.notes[0].text)
        assertEquals("Second valid note", result.notes[1].text)
        assertTrue(result.hadErrors)
    }

    @Test
    fun testListNoteItem_survivesSerializeParseRoundTrip() {
        val listNote = NoteItem(
            id = "list-1",
            text = "1. Milk\n2. Eggs",
            type = NoteType.LIST,
            title = "Groceries",
            listItems = mutableListOf(
                ListRowEntry(id = "r1", value = "Milk", insertionOrder = 0),
                ListRowEntry(id = "r2", value = "Eggs", insertionOrder = 1)
            )
        )
        val json = notesToJson(listOf(listNote))
        val parseResult = parseNotesFromJson(json)

        assertEquals(1, parseResult.notes.size)
        val parsed = parseResult.notes[0]
        assertEquals("list-1", parsed.id)
        assertEquals("1. Milk\n2. Eggs", parsed.text)
        assertEquals(NoteType.LIST, parsed.type)
        assertEquals("Groceries", parsed.title)
        assertEquals(2, parsed.listItems.size)
        assertEquals("Milk", parsed.listItems[0].value)
        assertEquals("Eggs", parsed.listItems[1].value)
        assertEquals(0, parsed.listItems[0].insertionOrder)
        assertEquals(1, parsed.listItems[1].insertionOrder)
        assertTrue(!parseResult.hadErrors)
    }

    @Test
    fun testOldFormatJson_withNoListItems_parsesAsEmptyList() {
        val oldJson = """
            [
                {"id":"old-1","text":"Old note","createdAt":1000,"type":"NORMAL"}
            ]
        """.trimIndent()
        val result = parseNotesFromJson(oldJson)
        assertEquals(1, result.notes.size)
        assertTrue(result.notes[0].listItems.isEmpty())
        assertTrue(!result.hadErrors)
    }
}
