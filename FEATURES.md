# Messy Note — Feature Specifications

Describes actual implemented behavior verified against the Kotlin source code. All note writes to disk are synchronous.

## 1. Light/Dark Theme Preference & Circular Reveal
- **Trigger**: Tap `ThemeModeToggle` switch in `NormalHeader` (top-left beside app title).
- **Behavior**: Dual-layer circular reveal animation expanding from the measured toggle button center (`CircularRevealClipShape`) using `350ms` `FastOutSlowInEasing`. At 50% transition progress (`snapshotFlow { revealProgress.value }.first { it >= 0.5f }`), status and navigation bar icons toggle appearance via `WindowCompat.getInsetsController`. Once complete, the target theme commits to `committedDarkTheme` and the overlay layer disposes.
- **Interactions & Edge Cases**: Preference is saved to `SharedPreferences` (`"mess_note_preferences"`, key `"theme_mode"`, values `"dark"` or `"light"`); absent preference defaults to system dark-mode. Toggle is disabled during active theme transitions, open dialogs/sheets, expanded composer, FAB plus menu, search, multi-selection, or Favorites page. Both reveal layers share identical note state, draft, and scroll positions.

## 2. Feed Ordering & Chronological Display
- **Trigger**: Automatic on launch, note creation, editing, or filter/search changes.
- **Behavior**: Notes in `NotesList` are sorted newest-first by `createdAt` descending via `orderNotesNewestFirst`. Ties (equal timestamps) are broken by higher index in the storage list (later-stored first). Notes render using `SentNoteBubble` with stable keys (`type:id:occurrence`).
- **Interactions & Edge Cases**: Sorting is pure and immutable. Modifying a note updates `updatedAt` without changing its original `createdAt` position in the list.

## 3. Content Type Filters & Horizontal Swipe Gesture
- **Trigger**: Tap a segment in `HomeFilterControl` (`All`, `Notes`, `Fields-Values`, `Lists`) or swipe horizontally across the home list.
- **Behavior**: Filters visible notes by `NoteType`: `All` shows all notes; `Notes` shows `NoteType.NORMAL`; `Fields-Values` shows `NoteType.FIELD_VALUE`; `Lists` shows `NoteType.LIST`. Tapping or swiping triggers subtle haptic feedback (`TextHandleMove`) and resets list scroll position to item 0 via `LaunchedEffect(selectedFilter, searchQuery)`.
- **Gesture**: Horizontal swipe detection in `OneLineNotesScreen` requires exceeding touch slop and moving horizontally > 1.5x vertical movement. Reaching a 64dp threshold switches one adjacent filter (swipe left advances `All` → `Notes` → `Fields-Values` → `Lists`; swipe right reverses). Swiping does not wrap around edges. Vertical scroll retains priority.
- **Interactions & Edge Cases**: Switching filters immediately clears active multi-selection (`selectedNoteIds.clear()`) and collapses the FAB plus menu. Disabled during multi-selection mode and on the Favorites page.

## 4. Live Search (Home & Favorites)
- **Trigger**: Tap search icon in `NormalHeader` (home) or `FavoritesHeader` (favorites).
- **Behavior**: Header transitions to `SearchBarHeader` with auto-focused `BasicTextField` and clear `X` button. Filters active list in real-time. Matches case-insensitively across: note body text (`text`), title (`title`), field keys (`field`), field values (`value`), and list row entries (`listItems`).
- **Interactions & Edge Cases**: Opening search immediately dismisses selection mode, closes the FAB plus menu, and hides the composer/FAB overlay. IME action Search hides keyboard while keeping search query active. Tap back arrow or clear `X` empties query; back arrow also exits search mode. Sending a new note while search is nonblank clears query and exits search to reveal the new note.

## 5. Favorites Management & Undo
- **Trigger**: Star icon in `SingleNoteOptionsBottomSheet` toggles favorite status; star button in `NormalHeader` navigates to Favorites page.
- **Behavior**: Persists `favorite: Boolean` flag on `NoteItem`. Favorites page renders `NotesList` displaying only items where `favorite == true`, sorted newest-first.
- **Interactions & Edge Cases**: Navigating to Favorites page scrolls list to top (index 0); exiting back to home also resets home scroll to top. When removing a favorite from inside the Favorites page, a `SwipeableSnackbar` displays "Removed from favorites" with an "Undo" action. Tapping Undo immediately restores `favorite = true` and re-saves to disk.

## 6. Monospace Formatting & Quick-Copy Tap
- **Trigger**: Enclose text with markdown backticks (`` `code` ``) when sending, or select text and tap "Mono" in the floating `MonoTextToolbar`.
- **Behavior**: Spans are normalized into `TextSpan(start, end, "mono")` stored on `NoteItem.spans`. Monospace text renders with `FontFamily.Monospace`, `FontWeight.SemiBold`, background tint (`MonospaceBg`), and accent color (`MonospaceText`).
- **Quick-Copy**: Tapping directly on a formatted monospace substring inside a `SentNoteBubble` immediately copies that exact snippet to clipboard, triggers haptic feedback, and displays transient snackbar "Copied: <snippet>".
- **Interactions & Edge Cases**: Tapping monospace while multi-selection mode is active toggles note selection instead of copying. Editing note text dynamically adjusts span offsets via `adjustSpansForTextChange`.

## 7. Automatic URL Detection & Safe Open Confirmation
- **Trigger**: Typing or saving any valid web address (http://, https://, www., or valid domain).
- **Behavior**: `extractUrlSpans` uses `PatternsCompat.WEB_URL` to identify links, stripping trailing punctuation (`.`, `,`, `;`, `:`, `!`, `?`, `)`). URLs render with underline and `LinkAccent` color.
- **Safe Open Modal**: Tapping a detected URL inside a bubble opens an `AlertDialog` titled "Open link?" displaying the full URL. Tapping "Open" normalizes scheme (prefixes `https://` if missing) and launches `ACTION_VIEW` intent with `FLAG_ACTIVITY_NEW_TASK`. If no browser or handler app exists, displays "No application found to open link". Tapping "Cancel" dismisses dialog without action.
- **Interactions & Edge Cases**: URL taps are disabled when multi-select mode is active (taps select the note). Works inside normal notes, field keys, field values, and list rows.

## 8. Single Note Options Bottom Sheet
- **Trigger**: Single tap on any note bubble when not in multi-selection mode.
- **Behavior**: Modal sheet (`SingleNoteOptionsBottomSheet`) slides up from bottom with drag handle, favorite star toggle in header, and stacked action cards:
  1. **Copy**: Copies entire compiled grouped note text (`buildGroupedText()`) to clipboard and displays "Copied to clipboard!".
  2. **Copy partially**: Closes bottom sheet and opens `PartialCopyDialog`.
  3. **Edit**: Closes bottom sheet and opens type-specific editor (`EditNoteDialog`, `EditFieldValueGroupDialog`, or `EditListGroupDialog`).
  4. **Delete**: Closes bottom sheet and prompts `DeleteConfirmDialog`.
- **Interactions & Edge Cases**: Dragging down, tapping scrim, or pressing system Back dismisses the sheet cleanly.

## 9. Partial Copy Dialog & Touch-Range Drag
- **Trigger**: "Copy partially" in bottom sheet or top bar action when exactly 1 note is selected.
- **Behavior**: Displays selectable token chips inside a scrollable modal container:
  - **Normal Notes**: Text is tokenized into whitespace-delimited words. URLs render as single chips (label truncated to 25 chars + `…`, but value remains complete URL).
  - **Field-Value Notes**: Tokenizes fields and values separately per row; row numbers and `=` are non-selectable static markers.
  - **List Notes**: Each row acts as a single selectable token.
- **Selection Gestures**: Single tap toggles individual chip selection. Long-press (>200ms) initiates drag range selection: dragging finger across chips selects or deselects ranges with automatic edge scrolling and custom `DraggableScrollbar`. "All" selects all tokens; "Clear" clears selection.
- **Copy Action**: "Copy Snippet" button concatenates selected words with spaces (or newlines for lists) and writes to clipboard. Disabled if selection is empty.

## 10. Collapsible Notes Composer & Backdrop Blur
- **Trigger**: Bottom input panel on home feed.
- **Behavior**: Multi-line `BasicTextField` with initial single-line height, expanding up to 4 lines (`maxLines = 4`). Glassmorphism container features translucent tint (`BgDark` at 78% opacity) and real-time backdrop blur (`BlurEffect` 14dp on Android 12+/API 31+ via graphics layer capture).
- **Expand Button**: When input text wraps or lays out to 2 or more lines (`inputLineCount > 1`), a circular expand icon (`R.drawable.ic_expand`) appears above the send button. Tapping it opens `ExpandedNoteComposer`.
- **Send Button**: Circular accent button. Trims input; if non-empty, parses markdown backticks, normalizes spans, creates `NoteItem`, saves to storage, clears composer, and animates list to item 0.
- **Interactions & Edge Cases**: Hidden during search, multi-selection, Favorites page, and when expanded. Draft text and spans survive filter switches.

## 11. Full-Screen Expanded Note Composer
- **Trigger**: Tap expand icon in `NotesComposer`.
- **Behavior**: Opens `ExpandedNoteComposer` full-screen editor overlay taking over viewport with status bar and navigation bar padding. Contains close button, "New note" title, and "Send" button.
- **Editing**: Multiline `OutlinedTextField` with unbounded lines (`maxLines = Int.MAX_VALUE`), monospace visual transformation, and floating `MonoTextToolbar`. Auto-requests focus on launch.
- **Interactions & Edge Cases**: Pressing close or system Back dismisses full-screen composer and restores text/spans into collapsed bottom composer without losing input. Tapping "Send" persists the note, dismisses keyboard, and closes the editor.

## 12. Field-Value Group Notes (Creation & Editing)
- **Trigger**: Creation via FAB plus menu → "Field-Value"; editing via bottom sheet / header Edit on `FIELD_VALUE` note.
- **Behavior**: Full-screen dialog with optional title and dynamic key-value rows. Each row contains numbered index, "Field" input, "=", "Value" input, and delete icon (when >1 row exists).
  - **Row Addition**: "+ Add" appends a new pair and focuses its field input. Blocked with inline error if current last row is completely blank.
  - **Row Deletion**: Delete icon removes row; disabled when only 1 row remains.
  - **Save Rules**: Trims all text. Discards rows where both field and value are blank. Requires at least one row with either field or value nonblank. Saves compiled text as `title\n\n1. field = value...`.
- **Interactions & Edge Cases**: Back or Close on untouched dialog exits immediately. If changes exist, triggers `DiscardConfirmDialog`.

## 13. List Group Notes (Creation & Editing)
- **Trigger**: Creation via FAB plus menu → "List"; editing via bottom sheet / header Edit on `LIST` note.
- **Behavior**: Full-screen dialog with optional title and dynamic numbered rows (`1.`, `2.`, etc.). Each row supports up to 2 lines of text.
  - **Row Addition**: "+ Add" appends a new row; disabled if current last row is empty.
  - **Row Deletion**: Delete button available when >1 row exists.
  - **Save Rules**: Requires first row to be nonblank. Trailing blank rows are pruned. Numbered rows saved sequentially and compiled to `1. Item\n2. Item`.
- **Interactions & Edge Cases**: Back or Close prompts `DiscardConfirmDialog` if title or row contents were modified.

## 14. Normal Note Full-Screen Editor
- **Trigger**: "Edit" in options sheet or selection header on a `NORMAL` note.
- **Behavior**: Full-screen `EditNoteDialog` with existing text and monospace spans preloaded (cursor placed at end of text). Selection supports `MonoTextToolbar` to toggle monospace styling.
- **Save Rules**: Trims text; empty string cannot be saved. On save, updates note `text`, `spans`, and sets `updatedAt = System.currentTimeMillis()`.
- **Interactions & Edge Cases**: Back or Close prompts `DiscardConfirmDialog` only if text or formatting spans differ from original note.

## 15. Multi-Selection Mode & Bulk Actions
- **Trigger**: Long-press any note bubble (or tap bubble when already in selection mode).
- **Behavior**: Enters multi-select mode. Top bar replaced by `MultiSelectHeader` displaying `<count> selected` and contextual action buttons:
  - **1 note selected**: `[Edit]` (opens type-specific editor), `[Copy]` (copies note text), `[Copy Partially]` (opens partial copy dialog), `[Delete]` (prompts delete confirmation).
  - **2+ notes selected**: `[Copy]` (copies all selected notes joined by `\n\n`), `[Delete]` (prompts bulk delete confirmation).
  - Selected notes display a blue circular checkmark icon beside the bubble.
- **Interactions & Edge Cases**: Long-pressing an already selected note deselects it. When selection count drops to 0, multi-select mode exits. System Back or top-left `X` clears selection. Bottom composer, FAB plus menu, and filter swipe gesture are disabled while multi-select is active.

## 16. Delete Confirmation & Undo Restoration
- **Trigger**: Delete action in options bottom sheet or multi-select header.
- **Behavior**: Modal `DeleteConfirmDialog` asks "Delete 1 note?" or "Delete <N> notes?". Confirming removes notes from active list and rewrites `one_line_notes.json`.
- **Undo Restoration**: Deletion posts `SwipeableSnackbar` with "Undo" button. Tapping Undo restores deleted notes back into their exact prior list indices (`sortedBy { it.first }`) and immediately re-saves storage file. Swiping snackbar away or letting it expire makes deletion permanent.

## 17. FAB Plus Menu, Empty States & Back Navigation Priority
- **FAB Plus Menu**: Floating action button with `+` icon expands vertically with animated pills: "Field-Value" and "List". Tapping outside, switching filters, opening search, or pressing Back collapses the menu.
- **Empty States**: Centered illustration icon and contextual text: "No notes yet", "No Fields-Values yet", "No Lists yet", "No matching favorites", or "No notes matching '<query>'".
- **Back Navigation Priority**: Handled in strict hierarchical order by `BackHandler`:
  1. Close expanded note composer (`isComposerExpanded = false`).
  2. Close FAB plus menu (`isPlusMenuExpanded = false`).
  3. Clear multi-selection mode (`selectedNoteIds.clear()`).
  4. Exit search and clear query (`isSearchActive = false`).
  5. Dismiss open bottom sheet or delete dialog.
  6. Exit Favorites page back to home feed.

## 18. Persistence Scope & Deliberately Absent Capabilities
- **Storage Scope**: Notes JSON persists note content, IDs, timestamps, spans, type, title, child entries, and favorite flags. SharedPreferences persists light/dark mode preference.
- **Not Persisted**: Search queries, selected filter tab, multi-selection state, plus menu state, dialog input drafts, scroll positions, composer input draft text and spans.
- **Absent Capabilities**: Cloud sync, user accounts, import/export files, rich text (bold/italics/headings beyond monospace), note folders/tags, note pinning, home screen widgets.
