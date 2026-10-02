package com.oneline.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteFilterTest {

    private val normalNote1 = NoteItem(
        id = "1",
        text = "Buy groceries",
        type = NoteType.NORMAL
    )

    private val normalNote2 = NoteItem(
        id = "2",
        text = "Call dentist tomorrow",
        type = NoteType.NORMAL
    )

    private val fieldValueNote1 = NoteItem(
        id = "3",
        text = "Server = Production",
        type = NoteType.FIELD_VALUE,
        fieldItems = mutableListOf(FieldValueEntry(field = "Server", value = "Production", insertionOrder = 0))
    )

    private val fieldValueNote2 = NoteItem(
        id = "4",
        text = "API_KEY = secret123",
        type = NoteType.FIELD_VALUE,
        fieldItems = mutableListOf(FieldValueEntry(field = "API_KEY", value = "secret123", insertionOrder = 0))
    )

    private val allNotes = listOf(normalNote1, normalNote2, fieldValueNote1, fieldValueNote2)

    @Test
    fun testFilterAll_returnsAllNotes() {
        val filtered = filterNotes(allNotes, NoteFilter.ALL)
        assertEquals(4, filtered.size)
        assertEquals(allNotes, filtered)
    }

    @Test
    fun testFilterNotes_returnsOnlyNormalNotes() {
        val filtered = filterNotes(allNotes, NoteFilter.NOTES)
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.type == NoteType.NORMAL })
        assertEquals(listOf(normalNote1, normalNote2), filtered)
    }

    @Test
    fun testFilterFieldsValues_returnsOnlyFieldValueNotes() {
        val filtered = filterNotes(allNotes, NoteFilter.FIELDS_VALUES)
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.type == NoteType.FIELD_VALUE })
        assertEquals(listOf(fieldValueNote1, fieldValueNote2), filtered)
    }

    @Test
    fun testFilterEmptyList_returnsEmpty() {
        val empty = emptyList<NoteItem>()
        assertTrue(filterNotes(empty, NoteFilter.ALL).isEmpty())
        assertTrue(filterNotes(empty, NoteFilter.NOTES).isEmpty())
        assertTrue(filterNotes(empty, NoteFilter.FIELDS_VALUES).isEmpty())
    }

    @Test
    fun testFilterWithSearchQuery_onAll() {
        val filtered = filterNotes(allNotes, NoteFilter.ALL, "dentist")
        assertEquals(1, filtered.size)
        assertEquals("2", filtered[0].id)

        val filteredField = filterNotes(allNotes, NoteFilter.ALL, "Production")
        assertEquals(1, filteredField.size)
        assertEquals("3", filteredField[0].id)
    }

    @Test
    fun testFilterWithSearchQuery_onNotesHidesFieldValueMatches() {
        // "Production" matches fieldValueNote1, but filter is NOTES so it should not appear
        val filtered = filterNotes(allNotes, NoteFilter.NOTES, "Production")
        assertTrue(filtered.isEmpty())

        // "groceries" matches normalNote1
        val filteredNormal = filterNotes(allNotes, NoteFilter.NOTES, "groceries")
        assertEquals(1, filteredNormal.size)
        assertEquals("1", filteredNormal[0].id)
    }

    @Test
    fun testFilterWithSearchQuery_onFieldsValuesHidesNormalMatches() {
        // "groceries" matches normalNote1, but filter is FIELDS_VALUES
        val filtered = filterNotes(allNotes, NoteFilter.FIELDS_VALUES, "groceries")
        assertTrue(filtered.isEmpty())

        // "API_KEY" matches fieldValueNote2
        val filteredField = filterNotes(allNotes, NoteFilter.FIELDS_VALUES, "API_KEY")
        assertEquals(1, filteredField.size)
        assertEquals("4", filteredField[0].id)
    }

    @Test
    fun testFilterEnumLabels() {
        assertEquals("All", NoteFilter.ALL.label)
        assertEquals("Notes", NoteFilter.NOTES.label)
        assertEquals("Fields-Values", NoteFilter.FIELDS_VALUES.label)
    }

    @Test
    fun testGroupedFieldValueNote_searchMatching() {
        val groupedNote = NoteItem(
            id = "5",
            text = "Employee Details\n\nName = John\nEmployee ID = EMP-1024\nEmail = john@example.com",
            type = NoteType.FIELD_VALUE,
            title = "Employee Details",
            fieldItems = mutableListOf(
                FieldValueEntry(field = "Name", value = "John", insertionOrder = 0),
                FieldValueEntry(field = "Employee ID", value = "EMP-1024", insertionOrder = 1),
                FieldValueEntry(field = "Email", value = "john@example.com", insertionOrder = 2)
            )
        )
        val testList = allNotes + groupedNote

        // Match on group title
        val matchTitle = filterNotes(testList, NoteFilter.ALL, "Employee Details")
        assertEquals(1, matchTitle.size)
        assertEquals("5", matchTitle[0].id)

        // Match on field name
        val matchField = filterNotes(testList, NoteFilter.ALL, "Employee ID")
        assertEquals(1, matchField.size)
        assertEquals("5", matchField[0].id)

        // Match on value
        val matchValue = filterNotes(testList, NoteFilter.ALL, "EMP-1024")
        assertEquals(1, matchValue.size)
        assertEquals("5", matchValue[0].id)

        // In Fields-Values filter, matches
        val matchFilterFV = filterNotes(testList, NoteFilter.FIELDS_VALUES, "john@example.com")
        assertEquals(1, matchFilterFV.size)
        assertEquals("5", matchFilterFV[0].id)

        // In Notes filter, hidden
        val matchFilterNotes = filterNotes(testList, NoteFilter.NOTES, "EMP-1024")
        assertTrue(matchFilterNotes.isEmpty())
    }

    @Test
    fun testBuildGroupedText_preservesInsertionOrder() {
        val groupedNote = NoteItem(
            id = "6",
            text = "",
            type = NoteType.FIELD_VALUE,
            title = "Server Config",
            fieldItems = mutableListOf(
                FieldValueEntry(field = "Port", value = "8080", insertionOrder = 2),
                FieldValueEntry(field = "Host", value = "127.0.0.1", insertionOrder = 0),
                FieldValueEntry(field = "SSL", value = "Enabled", insertionOrder = 1)
            )
        )
        val formatted = groupedNote.buildGroupedText()
        val expected = "Server Config\n\n1. Host = 127.0.0.1\n2. SSL = Enabled\n3. Port = 8080"
        assertEquals(expected, formatted)
    }

    @Test
    fun testBuildGroupedText_singlePair_noNumbering() {
        val singlePairNote = NoteItem(
            id = "7",
            text = "",
            type = NoteType.FIELD_VALUE,
            title = "Server Config",
            fieldItems = mutableListOf(
                FieldValueEntry(field = "Host", value = "127.0.0.1", insertionOrder = 0)
            )
        )
        val formatted = singlePairNote.buildGroupedText()
        val expected = "Server Config\n\nHost = 127.0.0.1"
        assertEquals(expected, formatted)
    }

    @Test
    fun testFilterLists_returnsOnlyListNotes() {
        val listNote = NoteItem(
            id = "5",
            text = "1. Apples\n2. Bananas",
            type = NoteType.LIST,
            title = "Fruits",
            listItems = mutableListOf(
                ListRowEntry(value = "Apples", insertionOrder = 0),
                ListRowEntry(value = "Bananas", insertionOrder = 1)
            )
        )
        val mixedNotes = allNotes + listNote
        val filtered = filterNotes(mixedNotes, NoteFilter.LISTS)
        assertEquals(1, filtered.size)
        assertEquals(NoteType.LIST, filtered[0].type)
        assertEquals("5", filtered[0].id)
    }

    @Test
    fun testSearchIncludesListNotesRowTextAndTitle() {
        val listNote = NoteItem(
            id = "5",
            text = "1. Apples\n2. Bananas",
            type = NoteType.LIST,
            title = "Fruit Shopping",
            listItems = mutableListOf(
                ListRowEntry(value = "Apples", insertionOrder = 0),
                ListRowEntry(value = "Bananas", insertionOrder = 1)
            )
        )
        val mixedNotes = allNotes + listNote

        // Matching row text "Bananas"
        val matchRow = filterNotes(mixedNotes, NoteFilter.ALL, "Bananas")
        assertEquals(1, matchRow.size)
        assertEquals("5", matchRow[0].id)

        // Matching title "Shopping"
        val matchTitle = filterNotes(mixedNotes, NoteFilter.ALL, "Shopping")
        assertEquals(1, matchTitle.size)
        assertEquals("5", matchTitle[0].id)

        // On NoteFilter.LISTS matching row text
        val matchInListFilter = filterNotes(mixedNotes, NoteFilter.LISTS, "Apples")
        assertEquals(1, matchInListFilter.size)
        assertEquals("5", matchInListFilter[0].id)
    }
}
