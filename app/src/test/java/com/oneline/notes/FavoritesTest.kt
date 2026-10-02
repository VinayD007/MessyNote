package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesTest {

    @Test
    fun testJsonWithNoFavoriteFlagParsesAsFalse() {
        val json = """
            [
                {"id":"note1","text":"Note 1","createdAt":1000,"type":"NORMAL"},
                {"id":"note2","text":"","createdAt":2000,"type":"FIELD_VALUE","items":[{"field":"Host","value":"localhost","insertionOrder":0}]}
            ]
        """.trimIndent()

        val result = parseNotesFromJson(json)
        assertEquals(2, result.notes.size)
        assertFalse(result.hadErrors)
        assertFalse(result.notes[0].favorite)
        assertFalse(result.notes[1].favorite)
        assertEquals("Host", result.notes[1].fieldItems[0].field)
        assertEquals("localhost", result.notes[1].fieldItems[0].value)
    }

    @Test
    fun testFavoriteSurvivesSerializeParseRoundTrip() {
        val originalNotes = listOf(
            NoteItem(
                id = "fav1",
                text = "Favorite Note",
                createdAt = 1000,
                type = NoteType.NORMAL,
                favorite = true
            ),
            NoteItem(
                id = "not_fav",
                text = "Regular Note",
                createdAt = 2000,
                type = NoteType.NORMAL,
                favorite = false
            ),
            NoteItem(
                id = "fav2",
                text = "Server = prod",
                createdAt = 3000,
                type = NoteType.FIELD_VALUE,
                title = "Config",
                fieldItems = mutableListOf(FieldValueEntry(field = "Server", value = "prod")),
                favorite = true
            )
        )

        val json = notesToJson(originalNotes)
        val parseResult = parseNotesFromJson(json)

        assertEquals(3, parseResult.notes.size)
        assertFalse(parseResult.hadErrors)

        assertEquals(true, parseResult.notes[0].favorite)
        assertEquals(false, parseResult.notes[1].favorite)
        assertEquals(true, parseResult.notes[2].favorite)

        assertEquals("fav1", parseResult.notes[0].id)
        assertEquals("not_fav", parseResult.notes[1].id)
        assertEquals("fav2", parseResult.notes[2].id)
    }

    @Test
    fun testFavoritesForReturnsOnlyFavoritesInInputOrderFilteredByQuery() {
        val note1 = NoteItem(
            id = "1",
            text = "Buy apples and milk",
            type = NoteType.NORMAL,
            favorite = true
        )
        val note2 = NoteItem(
            id = "2",
            text = "Buy apples",
            type = NoteType.NORMAL,
            favorite = false
        )
        val note3 = NoteItem(
            id = "3",
            text = "Server = apple.com",
            type = NoteType.FIELD_VALUE,
            title = "Apple Servers",
            fieldItems = mutableListOf(FieldValueEntry(field = "Server", value = "apple.com")),
            favorite = true
        )
        val note4 = NoteItem(
            id = "4",
            text = "Meeting with dentist",
            type = NoteType.NORMAL,
            favorite = true
        )
        val note5 = NoteItem(
            id = "5",
            text = "Database = postgres",
            type = NoteType.FIELD_VALUE,
            title = "DB",
            fieldItems = mutableListOf(FieldValueEntry(field = "Database", value = "postgres")),
            favorite = false
        )

        val notes = listOf(note1, note2, note3, note4, note5)

        // Without query: returns only favorites, in preserved input order
        val allFavorites = favoritesFor(notes, "")
        assertEquals(listOf(note1, note3, note4), allFavorites)

        // With blank query: returns only favorites, in preserved input order
        val blankQueryFavorites = favoritesFor(notes, "   ")
        assertEquals(listOf(note1, note3, note4), blankQueryFavorites)

        // Filtered by query "apple": matches note1 (text) and note3 (title/field/value/text), but NOT note2 (not fav)
        val appleFavorites = favoritesFor(notes, "apple")
        assertEquals(listOf(note1, note3), appleFavorites)

        // Filtered by query "dentist": matches note4
        val dentistFavorites = favoritesFor(notes, "dentist")
        assertEquals(listOf(note4), dentistFavorites)

        // Filtered by query that does not match any favorite:
        val noMatchFavorites = favoritesFor(notes, "xyz123")
        assertTrue(noMatchFavorites.isEmpty())

        // Query matching non-favorite note only: returns empty list
        val nonFavQueryFavorites = favoritesFor(notes, "postgres")
        assertTrue(nonFavQueryFavorites.isEmpty())
    }
}
