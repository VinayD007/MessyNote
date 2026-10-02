# Project Context: Messy Note (One-Line Notes Android)

## 1. Application Identity & Tech Stack
- **App Name**: Messy Note (`@string/app_name`)
- **Package / Application ID**: `com.oneline.notes` (Version 1.0, VersionCode 1)
- **Language & Runtime**: Kotlin 2.2.10, JVM Target 11, Java 11 compatibility
- **Framework**: Jetpack Compose with Material 3 Expressive (`MaterialExpressiveTheme`)
- **Key Dependencies**:
  - Android Gradle Plugin: `9.1.0`
  - Compose BOM: `2026.02.01`
  - Material 3: `1.5.0-alpha18`
  - AndroidX Core KTX: `1.10.1` | Activity Compose: `1.8.0` | Lifecycle Runtime KTX: `2.6.1`
  - org.json: `20231013` | JUnit: `4.13.2`
- **SDK Target**: Min SDK 26, Target SDK 36, Compile SDK 36 (minorApiLevel 1)
- **Gradle Commands**:
  - Build: `./gradlew assembleDebug`
  - Test: `./gradlew test`
  - Install: `./gradlew installDebug`

---

## 2. Architecture & File Structure
Single-activity architecture (`MainActivity`) using Compose state (`remember`, `mutableStateOf`, `mutableStateListOf`, `derivedStateOf`). No ViewModel, MVI library, or Navigation Component.

### Source Layout (`app/src/main/java/com/oneline/notes/`):
- `MainActivity.kt`: Entry activity (`MainActivity`), root composable (`OneLineNotesScreen`), dialog/sheet state flags, back press hierarchy, and filter swipe detection.
- `NoteModels.kt`: Data models (`NoteItem`, `FieldValueEntry`, `ListRowEntry`), enums (`NoteType`, `NoteFilter`), pure ordering/filtering helpers (`getDisplayedNotes`, `getDisplayedFavorites`, `orderNotesNewestFirst`), and text formatters.
- `NoteStorage.kt`: JSON file persistence (`saveNotesToStorage`, `loadNotesFromStorage`, `notesToJson`, `parseNotesFromJson`), corrupt file backup, atomic write via `.tmp` rename, and `formatTime`.
- `NoteBubble.kt`: Sent note bubble UI (`SentNoteBubble`), selection animation/styling, tap-to-copy monospace snippets, tap-to-open URL confirmation dialog, and favorite star indicator.
- `NoteComponents.kt`: Top headers (`NormalHeader`, `FavoritesHeader`, `SearchBarHeader`, `MultiSelectHeader`), connected filter button group (`HomeFilterControl`), bottom composer (`NotesComposer`), list viewport (`NotesList`), empty states (`NotesEmptyState`), and dismissible snackbar (`SwipeableSnackbar`).
- `NoteDialogs.kt`: Modals and sheets:
  - `SingleNoteOptionsBottomSheet`: Options menu for individual note actions.
  - `PartialCopyDialog`: Word/row tokenization dialog with touch drag-selection, auto-scrolling, live snippet preview, and draggable scrollbar.
  - `NewFieldValueGroupDialog` & `EditFieldValueGroupDialog`: Field-value creation/editing with dynamic rows and inline title.
  - `NewListGroupDialog` & `EditListGroupDialog`: Ordered list creation/editing with single-field rows and inline title.
  - `EditNoteDialog`: Normal text editor with custom monospace toolbar integration.
  - `DeleteConfirmDialog`: Confirmation modal for single or bulk note deletion.
  - `DiscardConfirmDialog`: Alert guard for unsaved changes.
- `MonospaceUtils.kt`: Span models (`TextSpan`), span normalization/merging (`normalizeSpans`), toggle logic (`toggleMonospaceSpan`), text edit adjustment (`adjustSpansForTextChange`), backtick markdown parser (`parseMarkdownBackticks`), and JSON serialization helpers.
- `MonoTextToolbar.kt`: Custom `TextToolbar` implementation wrapping Android's native `ActionMode.Callback2` (floating type) to expose the app's "Mono" action without reflection.
- `UrlUtils.kt`: Web URL extraction using `PatternsCompat.WEB_URL` (`extractUrlSpans`), URL scheme normalization (`normalizeUrlForIntent`), external intent launch helper (`launchUrl`), and partial-copy URL tokenization (`tokenizeForPartialCopy`).
- `ui/theme/`:
  - `Theme.kt`: `OneLineNotesTheme`, setting status/nav bar colors and forcing dark appearance.
  - `Color.kt`: `MessNoteColors` and `DarkMessNoteColors` palette.
  - `Type.kt`: Google Sans font family (`regular`, `medium`, `bold`) mapped to Material 3 `Typography`.

---

## 3. Data Model
Defined in `NoteModels.kt` and `MonospaceUtils.kt`:

- **`NoteType` (Enum)**: `NORMAL`, `FIELD_VALUE`, `LIST`
- **`NoteFilter` (Enum)**: `ALL` ("All"), `NOTES` ("Notes"), `FIELDS_VALUES` ("Fields-Values"), `LISTS` ("Lists")
- **`NoteItem` (Data Class)**:
  - `id: String` (UUID)
  - `text: String` (Plain text body or compiled row text)
  - `createdAt: Long` (Epoch millis, default current time)
  - `updatedAt: Long?` (Epoch millis when edited, default `null`)
  - `spans: MutableList<TextSpan>` (Formatting ranges for `NORMAL` and `LIST` notes)
  - `type: NoteType` (Default `NoteType.NORMAL`)
  - `title: String?` (Optional group title for `FIELD_VALUE` or `LIST` notes)
  - `fieldItems: MutableList<FieldValueEntry>` (Child entries for `FIELD_VALUE` notes)
  - `listItems: MutableList<ListRowEntry>` (Child entries for `LIST` notes)
  - `favorite: Boolean` (Favorite flag, default `false`)
- **`FieldValueEntry` (Data Class)**: `id: String`, `field: String`, `value: String`, `insertionOrder: Int`
- **`ListRowEntry` (Data Class)**: `id: String`, `value: String`, `insertionOrder: Int`
- **`TextSpan` (Data Class)**: `start: Int`, `end: Int`, `type: String` (default `"mono"`)

---

## 4. Theme & Design System
- **Dark Mode Only**: The app is strictly designed for and runs in Dark Mode (`OneLineNotesTheme`). There is no light mode or theme switcher.
- **Edge-to-Edge System Bars**: Status bar colored to `HeaderBg` and navigation bar colored to `BgDark` (`isAppearanceLightStatusBars = false`, `isAppearanceLightNavigationBars = false`).
- **Typography**: Google Sans (`res/font/`) for all UI text; system `FontFamily.Monospace` for monospace spans, field value chips, and partial copy previews.
- **Color Palette (`DarkMessNoteColors`)**:
  - `bg`: `#14151B` (Root screen background)
  - `headerBg`: `#181A21` (Top bar header background)
  - `surfaceCard`: `#1E222B` (Note bubbles, filter chips, composer)
  - `surfaceDark`: `#252A35` (Dialog containers, bottom sheet surface)
  - `surfaceElevated`: `#252A35` (Dialog sub-cards, snackbar container)
  - `primaryBlue`: `#1D9BF0` (Send button, FAB, active chips, confirm buttons)
  - `brightBlue`: `#1D9BF0` (Accents, active borders, cursor, toolbar actions)
  - `selectedItemBackground`: `#1E3250` (Active selection note bubble highlight)
  - `destructiveAction`: `#EF5350` (Delete actions, error messages, remove icons)
  - `monospaceBg`: `#121620` (Monospace background highlight)
  - `monospaceText`: `#648EEA` (Monospace font color)
  - `textPrimary`: `#EDEDEF` (Primary headings, note content text)
  - `textSecondary` / `textMuted`: `#A0A2A9` (Labels, icons, timestamps, placeholders)
  - `borderSubtle`: `#2E3440` (Subtle dividers and chip borders)
  - Link accent: `#004296` (Underlined auto-detected hyperlinks)

---

## 5. Currently Implemented Features

### 5.1. Note Creation & Entry
- **Bottom Composer (`NotesComposer`)**:
  - Rounded pill input field with circular Send button.
  - Supports markdown backticks (e.g. `` `code` ``) automatically parsed into clean text with a `TextSpan` on send.
  - "Mono" formatting action in text selection toolbar via `MonoTextToolbar`.
  - Automatically trims outer whitespace and adjusts spans on send; scrolls list to top.
- **Expandable "+" FAB**:
  - Toggles open to show "Field-Value" and "List" creation pills.

### 5.2. Note Types & Bubble Rendering (`SentNoteBubble`)
- **Normal Notes**: Plain text with embedded monospace spans and auto-detected URLs. Tapping a monospace span copies it immediately ("Copied: <snippet>"). Tapping a URL prompts an "Open link?" dialog before launching via external browser.
- **Field-Value Notes**: Optional title + divider, followed by numbered rows `${index + 1}. <field> = <value>`. Value is displayed in a monospace chip; tapping the value chip copies it immediately.
- **List Notes**: Optional title + divider, followed by numbered rows `${index + 1}. <item>`. Supports monospace formatting and auto-linked URLs.
- **Metadata**: Right-aligned timestamp (`h:mm a`) and favorite star indicator.

### 5.3. Search & Tabs
- **Tabs (`HomeFilterControl`)**: Connected pill toggle buttons (`All`, `Notes`, `Fields-Values`, `Lists`). Horizontal swipe on the note list switches between tabs.
- **Search (`SearchBarHeader`)**: Replaces header with search input and clear button. Performs real-time case-insensitive filtering across note text, titles, field names, field values, and list items. Hides composer and FAB during search.

### 5.4. Favorites Page
- Accessed via Star icon in `NormalHeader`. Shows favorited notes sorted newest-first (`orderNotesNewestFirst`).
- Includes dedicated `FavoritesHeader` and search bar ("Search favorites...").
- Removing a favorite shows a snackbar with an "Undo" action.

### 5.5. Multi-Selection & Bulk Actions
- Long-press any note bubble to enter multi-select mode.
- Top bar switches to `MultiSelectHeader` showing selected count.
- **Actions**:
  - Exactly 1 note: Edit, Copy, Copy Partially, Delete.
  - 2+ notes: Copy (concatenates with `\n\n`), Delete.
- Note bubble highlights with `SelectedItemBackground`, border highlight, and checkmark.

### 5.6. Partial Copy (`PartialCopyDialog`)
- **Normal Notes**: Tokenized into word chips with tap-selection and touch drag-to-select with edge auto-scrolling. Detected URLs show truncated (~25 chars + "…") but copy the full URL.
- **Field-Value Notes**: Tokenized into separate field and value chips per row.
- **List Notes**: Selectable numbered row chips.
- **Controls**: "All", "Clear", live snippet preview with character/word/row counts, draggable scrollbar, and "Copy Snippet" button.

### 5.7. Monospace Formatting
- Spans represented by `TextSpan(start, end, type = "mono")`. Normalized and merged via `normalizeSpans`.
- Native floating `ActionMode.Callback2` toolbar (`MonoTextToolbar`) provides the "Mono" action.
- Automatic span offset recalculation during edits/trims via `adjustSpansForTextChange`.

### 5.8. Dialogs & Guards
- `EditNoteDialog`, `NewFieldValueGroupDialog` / `EditFieldValueGroupDialog`, and `NewListGroupDialog` / `EditListGroupDialog` guard unsaved changes with `DiscardConfirmDialog`.
- `DeleteConfirmDialog`: Unified confirmation for 1 or N notes.
- `SingleNoteOptionsBottomSheet`: Copy, Copy partially, Edit, Delete, Favorite toggle.

### 5.9. Snackbars & Position-Restoring Undo
- Floating `SwipeableSnackbar` anchored above the composer/FAB (or bottom of screen when hidden). Dismissible via horizontal swipe.
- Deleting notes shows an "Undo" action that restores deleted notes to their exact previous indices.

### 5.10. Back Navigation Hierarchy
System back gesture handles in prioritized order:
1. Close expandable "+" FAB menu.
2. Exit multi-selection mode.
3. Exit search mode (home search or favorites search).
4. Dismiss open bottom sheet or dialogs.
5. Exit Favorites page and return to Home.
6. System exit.

---

## 6. Storage & Persistence
- **Internal File**: JSON file at `context.filesDir/one_line_notes.json`.
- **Atomic Writes**: Writes to `.tmp` file, then renames to target file.
- **Corrupt Backup**: If parsing fails, copies file to `one_line_notes.corrupt-<timestamp>.json` without crashing.
- **Welcome Cleanup**: Automatically purges sample welcome notes on startup.
- **Note Ordering**: Newest first by `createdAt`, breaking ties by later-stored first (`orderNotesNewestFirst`).

---

## 7. Technical Constraints
- **Synchronous File I/O**: `saveNotesToStorage` and `loadNotesFromStorage` run synchronously on the calling thread.
- **Context Menu Flag**: `ComposeFoundationFlags.isNewContextMenuEnabled = false` is set in `MainActivity.onCreate` to retain custom text toolbar hooks.
- **Local Storage Only**: Notes exist only in `context.filesDir`. App uninstall or data clearance removes all notes.

---

## 8. Planned / Future Work (NOT Yet Implemented)
*(Do not assume these exist in the codebase)*
- Room / SQLite database migration (with asynchronous coroutine I/O).
- Note export and import (JSON / Markdown backup and restore).
- Cloud synchronization and multi-device backup.
- User interface to view and restore from corrupt file backups.
- Note tags, folders, or color categories.
- Pin-to-top functionality (distinct from Favorites).
- Rich text formatting beyond Monospace (e.g., Bold, Italic, Strikethrough).
