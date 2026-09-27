# Feature Specifications: Messy Note

## 1. Quick Single-Line Composer
- Behavior: Typing into the pill-shaped bottom field and tapping the circular Send button (or keyboard Send) creates a plain-text note. The input and active formatting reset, the list scrolls to the bottom, and notes persist to disk. During selection mode, the composer is hidden but unsubmitted drafts are preserved.
- Entry-point: `NotesComposer` in `app/src/main/java/com/oneline/notes/NoteComponents.kt` and `onSendNote()` in `app/src/main/java/com/oneline/notes/MainActivity.kt`.
- State: Reads and clears `inputTextFieldValue` and `inputSpans`; appends to `notes`; animates `listState`.
- Edge cases & incomplete: Blank or whitespace-only inputs are ignored. Spans adjust automatically on paste or typing. Input is restricted to single-line.

## 2. Key-Value / Field Note Creation
- Behavior: Tapping the circular "+" button above the composer opens a modal dialog to input an optional title and multiple field-value pairs. Row delete button ("X") is visible only when more than one row exists (an invisible spacer preserves field widths when only one row remains). Tapping Save builds a structured FIELD_VALUE note and stores it.
- Entry-point: `NewFieldValueGroupDialog` in `app/src/main/java/com/oneline/notes/NoteDialogs.kt`.
- State: Reads and toggles `showNewFieldDialog`; appends new `NoteItem` to `notes`; scrolls `listState`.
- Edge cases & incomplete: Empty rows are stripped. Save requires at least one non-empty field or value. Standalone keys or values format cleanly with equal signs.

## 3. Note Editing
- Behavior: Choosing Edit from the bottom sheet or multi-select header opens `EditNoteDialog` (plain text) or `EditFieldValueGroupDialog` (field-value pairs). Saving updates text, spans, or pair items, setting the `updatedAt` timestamp in-place. Field-value editor only shows row delete ("X") when more than one row exists.
- Entry-point: `EditNoteDialog` and `EditFieldValueGroupDialog` in `app/src/main/java/com/oneline/notes/NoteDialogs.kt`.
- State: Reads `activeNote`, `showEditDialog`, `showEditFieldDialog`; modifies `notes` in-place; resets dialog states.
- Edge cases & incomplete: Blank text submissions are rejected. Monospace spans are re-normalized and clamped to new character lengths.

## 4. Note Deletion & Undo
- Behavior: Deleting notes (single note or any multi-selection size) prompts a single unified confirmation dialog. Confirming removes notes and triggers a swipeable undo snackbar. Tapping "Undo" restores all deleted notes back to their exact original list positions.
- Entry-point: `DeleteConfirmDialog` in `app/src/main/java/com/oneline/notes/NoteDialogs.kt` and `SwipeableSnackbar` in `app/src/main/java/com/oneline/notes/NoteComponents.kt`.
- State: Reads `notesToDelete`, `showDeleteConfirmDialog`; removes or re-inserts `NoteItem` into `notes`; manages `snackbarHostState`.
- Edge cases & incomplete: Restores original list positions across any selection count. Dismissing or letting the snackbar expire makes deletion final.

## 5. Multi-Selection Mode
- Behavior: Long-pressing any note bubble enters selection mode, showing checkmarks on the left, hiding the bottom composer (text field, Send, "+"), and closing the software keyboard. The top bar swaps to `MultiSelectHeader` showing selected count, Edit (if 1 selected), Copy, Copy Partially (if 1 selected), and Delete.
- Entry-point: `SentNoteBubble` onLongClick in `app/src/main/java/com/oneline/notes/NoteBubble.kt` and `MultiSelectHeader` in `app/src/main/java/com/oneline/notes/NoteComponents.kt`.
- State: Reads and modifies `selectedNoteIds`, `isMultiSelectMode`; back gesture handled by `BackHandler`.
- Edge cases & incomplete: Deselecting all notes exits selection mode. Selection clears automatically on filter change, search open/close, or back gesture. Half-typed composer drafts are kept intact.

## 6. Clipboard & Partial Copy
- Behavior: Notes can be copied in full from the options sheet or multi-select header. Tapping a monospace span copies that snippet directly. Tapping "Copy partially" opens `PartialCopyDialog`: for FIELD_VALUE notes, each pair is displayed on its own row with static number, left-aligned field chips, static "=", and right-aligned value chips (only field and value words are selectable); for NORMAL notes, words are presented in a flow layout.
- Entry-point: `PartialCopyDialog` in `app/src/main/java/com/oneline/notes/NoteDialogs.kt` and `SentNoteBubble` onMonoTap in `app/src/main/java/com/oneline/notes/NoteBubble.kt`.
- State: Reads note text and spans; writes to `LocalClipboardManager`; toggles `showPartialCopyDialog`.
- Edge cases & incomplete: In FIELD_VALUE partial copy, numbers and "=" cannot be selected. Snippet counter reflects selected words and character length. Empty text cannot be copied.

## 7. Search & Filtering
- Behavior: Pill-shaped filter chips switch between All, Notes, and Fields-Values. Swiping left or right on the notes list area (including empty state) moves between filters in order (All ↔ Notes ↔ Fields-Values without wrap). Tapping the search icon opens `SearchBarHeader` to filter in real-time across text, titles, keys, and values.
- Entry-point: `HomeFilterControl` and `SearchBarHeader` in `app/src/main/java/com/oneline/notes/NoteComponents.kt`, list swipe gesture in `app/src/main/java/com/oneline/notes/MainActivity.kt`.
- State: Reads and writes `selectedFilter`, `searchQuery`, `isSearchActive`; computes `displayedNotes`.
- Edge cases & incomplete: Filter swipe gesture is disabled while in selection mode. Filter changes clear active selections. Search query text shows an inline clear button.

## 8. Monospace Text Formatting
- Behavior: Selecting text in composer or edit inputs displays a Mono option on the floating text toolbar. Tapping Mono toggles monospace styling over the selection. Monospace formatting uses a dedicated monospace font distinct from Google Sans.
- Entry-point: `MonoTextToolbar` in `app/src/main/java/com/oneline/notes/MonoTextToolbar.kt` and `toggleMonospaceSpan` in `app/src/main/java/com/oneline/notes/MonospaceUtils.kt`.
- State: Reads text selection; writes `TextSpan` entries to `inputSpans` or edit dialog spans.
- Edge cases & incomplete: Overlapping spans merge automatically. Re-selecting styled text removes monospace. Uses reflection on Android internal toolbar classes.

## 9. Interactions
- Selection Mode vs Composer: Entering selection mode hides the composer and "+" button and closes the keyboard without discarding draft text; exiting selection mode restores the composer and draft.
- Selection Mode vs Search: The search icon is completely hidden in selection mode (the top header is replaced by `MultiSelectHeader`). Opening or closing search clears any active selection.
- Search vs Composer: While search is active, the bottom composer (text field, Send button) and the "+" button are completely hidden; exiting search restores them.
- Selection Mode vs Navigation & Gestures: Horizontal filter swipe gestures are disabled during selection mode. Changing filters automatically clears note selections.
- Deletion vs Undo: Single and bulk deletions share the same confirmation dialog and swipeable snackbar; Undo restores all deleted notes to their previous list indices.
- Partial Copy: FIELD_VALUE notes render fixed-layout rows where only field and value chips are selectable; NORMAL notes render a wrapping chip flow.
- Typography: All application and Material text styles use Google Sans; code snippets and monospace formatted spans preserve dedicated monospace styling.
