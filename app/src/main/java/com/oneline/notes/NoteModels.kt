package com.oneline.notes

import java.util.UUID

enum class NoteType {
    NORMAL,
    FIELD_VALUE,
    LIST
}

enum class NoteFilter(val label: String) {
    ALL("All"),
    NOTES("Notes"),
    FIELDS_VALUES("Fields-Values"),
    LISTS("Lists")
}

data class FieldValueEntry(
    val id: String = UUID.randomUUID().toString(),
    var field: String,
    var value: String,
    val insertionOrder: Int = 0
)

data class ListRowEntry(
    val id: String = UUID.randomUUID().toString(),
    var value: String,
    val insertionOrder: Int = 0
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null,
    var spans: MutableList<TextSpan> = mutableListOf(),
    var type: NoteType = NoteType.NORMAL,
    var title: String? = null,
    var fieldItems: MutableList<FieldValueEntry> = mutableListOf(),
    var listItems: MutableList<ListRowEntry> = mutableListOf(),
    var favorite: Boolean = false
)

fun NoteItem.getSortedFieldItems(): List<FieldValueEntry> {
    return fieldItems.sortedBy { it.insertionOrder }
}

fun NoteItem.getSortedListItems(): List<ListRowEntry> {
    return listItems.sortedBy { it.insertionOrder }
}

fun NoteItem.buildListText(): String {
    val sorted = getSortedListItems()
    return sorted.mapIndexed { idx, it -> "${idx + 1}. ${it.value}" }.joinToString("\n")
}

fun NoteItem.buildGroupedText(): String {
    if (type == NoteType.LIST) return buildListText()
    if (type != NoteType.FIELD_VALUE) return text
    val t = title?.trim() ?: ""
    val sorted = getSortedFieldItems()
    val rows = sorted.mapIndexed { idx, it ->
        val prefix = if (sorted.size > 1) "${idx + 1}. " else ""
        if (it.field.isNotEmpty() && it.value.isNotEmpty()) {
            "$prefix${it.field} = ${it.value}"
        } else if (it.field.isNotEmpty()) {
            "$prefix${it.field} ="
        } else if (it.value.isNotEmpty()) {
            "$prefix= ${it.value}"
        } else {
            ""
        }
    }.filter { it.isNotEmpty() }.joinToString("\n")
    return if (t.isNotEmpty() && rows.isNotEmpty()) "$t\n\n$rows" else if (t.isNotEmpty()) t else rows
}

fun filterNotes(
    notes: List<NoteItem>,
    filter: NoteFilter,
    searchQuery: String = ""
): List<NoteItem> {
    val baseList = when (filter) {
        NoteFilter.ALL -> notes
        NoteFilter.NOTES -> notes.filter { it.type == NoteType.NORMAL }
        NoteFilter.FIELDS_VALUES -> notes.filter { it.type == NoteType.FIELD_VALUE }
        NoteFilter.LISTS -> notes.filter { it.type == NoteType.LIST }
    }
    if (searchQuery.isBlank()) {
        return baseList
    }
    val q = searchQuery.trim()
    return baseList.filter { note ->
        if (note.type == NoteType.FIELD_VALUE) {
            (note.title?.contains(q, ignoreCase = true) == true) ||
            note.fieldItems.any {
                it.field.contains(q, ignoreCase = true) || it.value.contains(q, ignoreCase = true)
            } ||
            note.text.contains(q, ignoreCase = true)
        } else if (note.type == NoteType.LIST) {
            (note.title?.contains(q, ignoreCase = true) == true) ||
            note.listItems.any {
                it.value.contains(q, ignoreCase = true)
            } ||
            note.text.contains(q, ignoreCase = true)
        } else {
            note.text.contains(q, ignoreCase = true)
        }
    }
}

fun favoritesFor(
    notes: List<NoteItem>,
    query: String = ""
): List<NoteItem> {
    val favorites = notes.filter { it.favorite }
    if (query.isBlank()) {
        return favorites
    }
    val q = query.trim()
    return favorites.filter { note ->
        if (note.type == NoteType.FIELD_VALUE) {
            (note.title?.contains(q, ignoreCase = true) == true) ||
            note.fieldItems.any {
                it.field.contains(q, ignoreCase = true) || it.value.contains(q, ignoreCase = true)
            } ||
            note.text.contains(q, ignoreCase = true)
        } else if (note.type == NoteType.LIST) {
            (note.title?.contains(q, ignoreCase = true) == true) ||
            note.listItems.any {
                it.value.contains(q, ignoreCase = true)
            } ||
            note.text.contains(q, ignoreCase = true)
        } else {
            note.text.contains(q, ignoreCase = true)
        }
    }
}

/**
 * Orders notes from newest to oldest by [NoteItem.createdAt].
 * For ties (equal createdAt), notes that appear later in the stored/input list
 * are ordered first (later-stored first).
 *
 * This pure function returns a newly ordered list and does not mutate the input list.
 */
fun orderNotesNewestFirst(notes: List<NoteItem>): List<NoteItem> {
    return notes.mapIndexed { index, note -> index to note }
        .sortedWith(
            compareByDescending<Pair<Int, NoteItem>> { it.second.createdAt }
                .thenByDescending { it.first }
        )
        .map { it.second }
}

fun getDisplayedNotes(
    notes: List<NoteItem>,
    filter: NoteFilter,
    searchQuery: String = ""
): List<NoteItem> {
    return orderNotesNewestFirst(filterNotes(notes, filter, searchQuery))
}

fun getDisplayedFavorites(
    notes: List<NoteItem>,
    query: String = ""
): List<NoteItem> {
    return orderNotesNewestFirst(favoritesFor(notes, query))
}
