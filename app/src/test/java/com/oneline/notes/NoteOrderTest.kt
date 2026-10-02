package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteOrderTest {

    @Test
    fun testOrderNotes_newestFirst() {
        val oldNote = NoteItem(id = "1", text = "Old note", createdAt = 1000L)
        val middleNote = NoteItem(id = "2", text = "Middle note", createdAt = 2000L)
        val newestNote = NoteItem(id = "3", text = "Newest note", createdAt = 3000L)

        val input = listOf(oldNote, middleNote, newestNote)
        val ordered = orderNotesNewestFirst(input)

        assertEquals(listOf(newestNote, middleNote, oldNote), ordered)
    }

    @Test
    fun testOrderNotes_ties_laterStoredFirst() {
        val noteA = NoteItem(id = "a", text = "First stored", createdAt = 5000L)
        val noteB = NoteItem(id = "b", text = "Second stored", createdAt = 5000L)
        val noteC = NoteItem(id = "c", text = "Third stored", createdAt = 5000L)

        val input = listOf(noteA, noteB, noteC)
        val ordered = orderNotesNewestFirst(input)

        // Equal createdAt ties: later-stored first
        assertEquals(listOf(noteC, noteB, noteA), ordered)
    }

    @Test
    fun testOrderNotes_mixedTimestampsAndTies() {
        val note1 = NoteItem(id = "1", text = "Time 1000, idx 0", createdAt = 1000L)
        val note2 = NoteItem(id = "2", text = "Time 2000, idx 1", createdAt = 2000L)
        val note3 = NoteItem(id = "3", text = "Time 2000, idx 2", createdAt = 2000L)
        val note4 = NoteItem(id = "4", text = "Time 500, idx 3", createdAt = 500L)

        val input = listOf(note1, note2, note3, note4)
        val ordered = orderNotesNewestFirst(input)

        // Expected: note3 (2000, idx 2), note2 (2000, idx 1), note1 (1000, idx 0), note4 (500, idx 3)
        assertEquals(listOf(note3, note2, note1, note4), ordered)
    }

    @Test
    fun testOrderNotes_inputListNotMutated() {
        val note1 = NoteItem(id = "1", text = "Note 1", createdAt = 1000L)
        val note2 = NoteItem(id = "2", text = "Note 2", createdAt = 3000L)
        val note3 = NoteItem(id = "3", text = "Note 3", createdAt = 2000L)

        val mutableInput = mutableListOf(note1, note2, note3)
        val originalSnapshot = mutableInput.toList()

        val ordered = orderNotesNewestFirst(mutableInput)

        // Input list should still be identical to original snapshot
        assertEquals(originalSnapshot, mutableInput)
        // Output list should be sorted
        assertEquals(listOf(note2, note3, note1), ordered)
    }

    @Test
    fun testOrderNotes_emptyAndSingleItem() {
        val empty = emptyList<NoteItem>()
        assertEquals(emptyList<NoteItem>(), orderNotesNewestFirst(empty))

        val single = listOf(NoteItem(id = "single", text = "Only note", createdAt = 1000L))
        assertEquals(single, orderNotesNewestFirst(single))
    }
}
