package com.oneline.notes

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val STORAGE_FILE = "one_line_notes.json"

data class ParseNotesResult(
    val notes: List<NoteItem>,
    val hadErrors: Boolean
)

fun parseNotesFromJson(jsonString: String): ParseNotesResult {
    if (jsonString.isBlank()) {
        return ParseNotesResult(emptyList(), hadErrors = false)
    }
    var hadErrors = false
    val list = mutableListOf<NoteItem>()
    val array = try {
        JSONArray(jsonString)
    } catch (_: Exception) {
        return ParseNotesResult(emptyList(), hadErrors = true)
    }

    for (i in 0 until array.length()) {
        try {
            val obj = array.getJSONObject(i)
            val typeStr = obj.optString("type", NoteType.NORMAL.name)
            val type = try {
                NoteType.valueOf(typeStr)
            } catch (_: Exception) {
                NoteType.NORMAL
            }
            val title = if (obj.has("title")) obj.getString("title") else null

            val items = mutableListOf<FieldValueEntry>()
            if (obj.has("items")) {
                val itemsArray = obj.getJSONArray("items")
                for (j in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(j)
                    items.add(
                        FieldValueEntry(
                            id = itemObj.optString("id", UUID.randomUUID().toString()),
                            field = itemObj.getString("field"),
                            value = itemObj.getString("value"),
                            insertionOrder = itemObj.optInt("insertionOrder", j)
                        )
                    )
                }
            }
            items.sortBy { it.insertionOrder }

            val listRows = mutableListOf<ListRowEntry>()
            if (obj.has("listItems")) {
                val listItemsArray = obj.getJSONArray("listItems")
                for (j in 0 until listItemsArray.length()) {
                    try {
                        val itemObj = listItemsArray.getJSONObject(j)
                        listRows.add(
                            ListRowEntry(
                                id = itemObj.optString("id", UUID.randomUUID().toString()),
                                value = itemObj.getString("value"),
                                insertionOrder = itemObj.optInt("insertionOrder", j)
                            )
                        )
                    } catch (_: Exception) {
                        hadErrors = true
                    }
                }
            }
            listRows.sortBy { it.insertionOrder }

            val rawText = obj.getString("text")
            val finalText = if (type == NoteType.LIST && rawText.isEmpty() && listRows.isNotEmpty()) {
                listRows.mapIndexed { idx, it -> "${idx + 1}. ${it.value}" }.joinToString("\n")
            } else {
                rawText
            }

            val note = NoteItem(
                id = obj.optString("id", UUID.randomUUID().toString()),
                text = finalText,
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = if (obj.has("updatedAt")) obj.getLong("updatedAt") else null,
                spans = spansFromJson(obj.optJSONArray("spans")).toMutableList(),
                type = type,
                title = title ?: if (type == NoteType.FIELD_VALUE && items.isNotEmpty()) items.first().field else null,
                fieldItems = items,
                listItems = listRows,
                favorite = obj.optBoolean("favorite", false),
                isDeleted = obj.optBoolean("isDeleted", false)
            )
            list.add(note)
        } catch (_: Exception) {
            hadErrors = true
        }
    }
    return ParseNotesResult(list, hadErrors)
}

private fun backupCorruptFile(context: Context, file: File) {
    try {
        val timestamp = System.currentTimeMillis()
        var backupFile = File(context.filesDir, "one_line_notes.corrupt-$timestamp.json")
        var counter = 1
        while (backupFile.exists()) {
            backupFile = File(context.filesDir, "one_line_notes.corrupt-$timestamp-$counter.json")
            counter++
        }
        file.copyTo(backupFile, overwrite = false)
    } catch (_: Exception) {}
}

fun notesToJson(notes: List<NoteItem>): String {
    val array = JSONArray()
    notes.forEach { note ->
        val obj = JSONObject().apply {
            put("id", note.id)
            put("text", note.text)
            put("createdAt", note.createdAt)
            if (note.updatedAt != null) put("updatedAt", note.updatedAt)
            put("spans", spansToJson(note.spans))
            put("type", note.type.name)
            put("favorite", note.favorite)
            put("isDeleted", note.isDeleted)
            if (note.title != null) put("title", note.title)
            if (note.fieldItems.isNotEmpty()) {
                val itemsArray = JSONArray()
                note.fieldItems.forEach { itm ->
                    val itemObj = JSONObject().apply {
                        put("id", itm.id)
                        put("field", itm.field)
                        put("value", itm.value)
                        put("insertionOrder", itm.insertionOrder)
                    }
                    itemsArray.put(itemObj)
                }
                put("items", itemsArray)
            }
            if (note.listItems.isNotEmpty()) {
                val listArray = JSONArray()
                note.listItems.forEach { itm ->
                    val itemObj = JSONObject().apply {
                        put("id", itm.id)
                        put("value", itm.value)
                        put("insertionOrder", itm.insertionOrder)
                    }
                    listArray.put(itemObj)
                }
                put("listItems", listArray)
            }
        }
        array.put(obj)
    }
    return array.toString(2)
}

fun saveNotesToStorage(context: Context, notes: List<NoteItem>) {
    try {
        val targetFile = File(context.filesDir, STORAGE_FILE)
        val tempFile = File(context.filesDir, "$STORAGE_FILE.tmp")
        tempFile.writeText(notesToJson(notes))
        if (!tempFile.renameTo(targetFile)) {
            targetFile.delete()
            tempFile.renameTo(targetFile)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadNotesFromStorage(context: Context): List<NoteItem> {
    val file = File(context.filesDir, STORAGE_FILE)
    if (!file.exists()) return emptyList()
    return try {
        val jsonString = file.readText()
        val result = parseNotesFromJson(jsonString)
        if (result.hadErrors) {
            backupCorruptFile(context, file)
        }
        result.notes
    } catch (_: Exception) {
        backupCorruptFile(context, file)
        emptyList()
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
