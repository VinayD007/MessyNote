package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BinTest {

    @Test
    fun testIsDeletedDefaultsToFalse() {
        val note = NoteItem(text = "Hello")
        assertFalse(note.isDeleted)
    }

    @Test
    fun testJsonWithoutIsDeletedFlagParsesAsFalse() {
        val legacyJson = """
            [
                {"id":"1","text":"Active note","createdAt":1000,"type":"NORMAL","favorite":true},
                {"id":"2","text":"Another active","createdAt":2000,"type":"NORMAL"}
            ]
        """.trimIndent()
        val result = parseNotesFromJson(legacyJson)
        assertEquals(2, result.notes.size)
        assertFalse(result.hadErrors)
        assertFalse(result.notes[0].isDeleted)
        assertFalse(result.notes[1].isDeleted)
    }

    @Test
    fun testIsDeletedSurvivesSerializeParseRoundTrip() {
        val notes = listOf(
            NoteItem(
                id = "1",
                text = "Active note",
                isDeleted = false,
                favorite = true
            ),
            NoteItem(
                id = "2",
                text = "Deleted note",
                isDeleted = true,
                favorite = true,
                type = NoteType.FIELD_VALUE,
                title = "Config",
                fieldItems = mutableListOf(FieldValueEntry(field = "key", value = "val"))
            )
        )
        val json = notesToJson(notes)
        val result = parseNotesFromJson(json)
        assertEquals(2, result.notes.size)
        assertFalse(result.hadErrors)

        assertFalse(result.notes[0].isDeleted)
        assertTrue(result.notes[0].favorite)

        assertTrue(result.notes[1].isDeleted)
        assertTrue(result.notes[1].favorite)
        assertEquals("Config", result.notes[1].title)
        assertEquals(1, result.notes[1].fieldItems.size)
    }

    @Test
    fun testFilterNotesExcludesDeletedNotes() {
        val active = NoteItem(id = "1", text = "Active", isDeleted = false)
        val deleted = NoteItem(id = "2", text = "Deleted", isDeleted = true)
        val notes = listOf(active, deleted)

        val filteredAll = filterNotes(notes, NoteFilter.ALL)
        assertEquals(listOf(active), filteredAll)

        val filteredNotes = filterNotes(notes, NoteFilter.NOTES)
        assertEquals(listOf(active), filteredNotes)
    }

    @Test
    fun testFavoritesForExcludesDeletedNotes() {
        val activeFav = NoteItem(id = "1", text = "Active Fav", favorite = true, isDeleted = false)
        val deletedFav = NoteItem(id = "2", text = "Deleted Fav", favorite = true, isDeleted = true)
        val notes = listOf(activeFav, deletedFav)

        val favs = favoritesFor(notes)
        assertEquals(listOf(activeFav), favs)
    }

    @Test
    fun testBinNotesForReturnsOnlyDeletedNotesAndSupportsSearch() {
        val active = NoteItem(id = "1", text = "Active doc", isDeleted = false)
        val deleted1 = NoteItem(id = "2", text = "Deleted groceries list", isDeleted = true)
        val deleted2 = NoteItem(id = "3", text = "Deleted secret key", isDeleted = true)
        val notes = listOf(active, deleted1, deleted2)

        val allBin = binNotesFor(notes)
        assertEquals(listOf(deleted1, deleted2), allBin)

        val searchGroceries = binNotesFor(notes, "groceries")
        assertEquals(listOf(deleted1), searchGroceries)

        val searchNone = binNotesFor(notes, "nonexistent")
        assertTrue(searchNone.isEmpty())
    }

    @Test
    fun testRestoringPreservesAllMetadata() {
        val originalCreatedAt = 123456789L
        val originalSpans = mutableListOf(TextSpan(0, 4, "mono"))
        val originalFieldItems = mutableListOf(FieldValueEntry(field = "Host", value = "127.0.0.1"))
        val note = NoteItem(
            id = "note-abc",
            text = "Host = 127.0.0.1",
            createdAt = originalCreatedAt,
            spans = originalSpans,
            type = NoteType.FIELD_VALUE,
            title = "Server Info",
            fieldItems = originalFieldItems,
            favorite = true,
            isDeleted = true
        )

        // Restoring sets isDeleted = false
        val restored = note.copy(isDeleted = false)

        assertFalse(restored.isDeleted)
        assertTrue(restored.favorite)
        assertEquals(originalCreatedAt, restored.createdAt)
        assertEquals(originalSpans, restored.spans)
        assertEquals("Server Info", restored.title)
        assertEquals(originalFieldItems, restored.fieldItems)
    }
}
