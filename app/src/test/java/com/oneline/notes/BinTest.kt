package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BinTest {

    @Test
    fun testNoteItem_defaultIsDeletedFalse() {
        val note = NoteItem(text = "Hello world")
        assertFalse(note.isDeleted)
    }

    @Test
    fun testParseNotesFromJson_backwardCompatibility_defaultsToFalse() {
        val legacyJson = """
            [
                {"id":"legacy-1","text":"Old note without isDeleted","createdAt":1000,"type":"NORMAL","favorite":true}
            ]
        """.trimIndent()
        val result = parseNotesFromJson(legacyJson)
        assertEquals(1, result.notes.size)
        assertFalse(result.notes[0].isDeleted)
        assertTrue(result.notes[0].favorite)
    }

    @Test
    fun testSoftDeletedNote_serializationRoundTrip() {
        val note = NoteItem(
            id = "bin-1",
            text = "Trashed note",
            favorite = true,
            isDeleted = true,
            type = NoteType.FIELD_VALUE,
            title = "Config",
            fieldItems = mutableListOf(
                FieldValueEntry(field = "Host", value = "localhost")
            )
        )
        val json = notesToJson(listOf(note))
        val result = parseNotesFromJson(json)

        assertEquals(1, result.notes.size)
        val parsed = result.notes[0]
        assertEquals("bin-1", parsed.id)
        assertTrue(parsed.isDeleted)
        assertTrue(parsed.favorite)
        assertEquals("Config", parsed.title)
        assertEquals(1, parsed.fieldItems.size)
        assertEquals("Host", parsed.fieldItems[0].field)
        assertEquals("localhost", parsed.fieldItems[0].value)
    }

    @Test
    fun testFilteringExcludesSoftDeletedNotes() {
        val activeNormal = NoteItem(id = "1", text = "Active normal", type = NoteType.NORMAL, isDeleted = false)
        val deletedNormal = NoteItem(id = "2", text = "Deleted normal", type = NoteType.NORMAL, isDeleted = true)
        val activeField = NoteItem(id = "3", text = "Active field", type = NoteType.FIELD_VALUE, isDeleted = false)
        val deletedField = NoteItem(id = "4", text = "Deleted field", type = NoteType.FIELD_VALUE, isDeleted = true)
        val activeList = NoteItem(id = "5", text = "Active list", type = NoteType.LIST, isDeleted = false)
        val deletedList = NoteItem(id = "6", text = "Deleted list", type = NoteType.LIST, isDeleted = true)

        val allNotes = listOf(activeNormal, deletedNormal, activeField, deletedField, activeList, deletedList)

        // ALL filter
        val displayedAll = filterNotes(allNotes, NoteFilter.ALL)
        assertEquals(listOf(activeNormal, activeField, activeList), displayedAll)

        // NOTES filter
        val displayedNotes = filterNotes(allNotes, NoteFilter.NOTES)
        assertEquals(listOf(activeNormal), displayedNotes)

        // FIELDS_VALUES filter
        val displayedFields = filterNotes(allNotes, NoteFilter.FIELDS_VALUES)
        assertEquals(listOf(activeField), displayedFields)

        // LISTS filter
        val displayedLists = filterNotes(allNotes, NoteFilter.LISTS)
        assertEquals(listOf(activeList), displayedLists)
    }

    @Test
    fun testFavoritesExcludesSoftDeletedNotes() {
        val activeFav = NoteItem(id = "f1", text = "Active favorite", favorite = true, isDeleted = false)
        val deletedFav = NoteItem(id = "f2", text = "Deleted favorite", favorite = true, isDeleted = true)
        val activeNonFav = NoteItem(id = "f3", text = "Active non-favorite", favorite = false, isDeleted = false)

        val notes = listOf(activeFav, deletedFav, activeNonFav)
        val favs = favoritesFor(notes)

        assertEquals(1, favs.size)
        assertEquals("f1", favs[0].id)
    }

    @Test
    fun testBinNotesFor_andSearchInBin() {
        val note1 = NoteItem(id = "b1", text = "Grocery shopping list", isDeleted = true)
        val note2 = NoteItem(id = "b2", text = "Secret credentials", isDeleted = true)
        val note3 = NoteItem(id = "b3", text = "Active note", isDeleted = false)

        val notes = listOf(note1, note2, note3)

        val binAll = binNotesFor(notes)
        assertEquals(2, binAll.size)
        assertEquals(listOf("b1", "b2"), binAll.map { it.id })

        val binSearch = binNotesFor(notes, "grocery")
        assertEquals(1, binSearch.size)
        assertEquals("b1", binSearch[0].id)
    }

    @Test
    fun testRestoreNotePreservesAllMetadata() {
        val note = NoteItem(
            id = "restore-1",
            text = "Keep this safe",
            createdAt = 123456789L,
            favorite = true,
            spans = mutableListOf(TextSpan(0, 4, "mono")),
            type = NoteType.LIST,
            title = "My Items",
            listItems = mutableListOf(ListRowEntry(id = "r1", value = "Item 1")),
            isDeleted = true
        )

        // Restore note
        note.isDeleted = false

        assertFalse(note.isDeleted)
        assertTrue(note.favorite)
        assertEquals(123456789L, note.createdAt)
        assertEquals(1, note.spans.size)
        assertEquals("mono", note.spans[0].type)
        assertEquals(NoteType.LIST, note.type)
        assertEquals("My Items", note.title)
        assertEquals(1, note.listItems.size)
        assertEquals("Item 1", note.listItems[0].value)
    }

    @Test
    fun testSoftDeleteAndRestoreNotesHelpers() {
        val n1 = NoteItem(id = "1", text = "Note 1", isDeleted = false)
        val n2 = NoteItem(id = "2", text = "Note 2", isDeleted = false)
        val list = listOf(n1, n2)

        val softDeleted = softDeleteNotes(list, setOf("1"))
        assertTrue(softDeleted[0].isDeleted)
        assertFalse(softDeleted[1].isDeleted)

        val restored = restoreNotes(softDeleted, setOf("1"))
        assertFalse(restored[0].isDeleted)
        assertFalse(restored[1].isDeleted)
    }
}
