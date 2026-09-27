# Project Context: Messy Note (One-Line Notes Android)

## 1. Stack
- **Language**: Kotlin 2.2.10 (JVM target 11)
- **Framework**: Jetpack Compose (BOM 2026.02.01), Material 3 1.5.0-alpha01
- **Typography**: Google Sans (Regular/Medium/Bold in `res/font/`, set in `ui/theme/Type.kt`); monospace formatting preserves dedicated monospace font
- **Key Libraries**: AndroidX Core KTX 1.10.1, Activity Compose 1.8.0, Lifecycle Runtime KTX 2.6.1, org.json 20231013, JUnit 4.13.2
- **SDK Target**: Min SDK 26, Target SDK 36, Compile SDK 36 (minorApiLevel 1)
- **Commands**:
  - Build: `./gradlew assembleDebug`
  - Test: `./gradlew test`
  - Run/Install: `./gradlew installDebug`

## 2. Folder Structure
- `app/src/main/java/com/oneline/notes/`:
  - `MainActivity.kt`: Entry activity (`MainActivity`) and top-level screen composable (`OneLineNotesScreen`)
  - `NoteModels.kt`: Data classes (`NoteItem`, `FieldValueEntry`), enums (`NoteType`, `NoteFilter`), text builder, and filtering
  - `NoteStorage.kt`: JSON persistence (`saveNotesToStorage`, `loadNotesFromStorage`, `parseNotesFromJson`), corrupt file backup, atomic saving
  - `NoteBubble.kt`: Chat-style note bubble composable (`SentNoteBubble`)
  - `NoteComponents.kt`: Headers (`MultiSelectHeader`, `NormalHeader`, `SearchBarHeader`), filter chips (`HomeFilterControl`), composer (`NotesComposer`), snackbar (`SwipeableSnackbar`), empty state
  - `NoteDialogs.kt`: Dialogs and sheets (`FieldValueGroupDialog`, `NewFieldValueGroupDialog`, `EditFieldValueGroupDialog`, `PartialCopyDialog`, `EditNoteDialog`, `DeleteConfirmDialog`, `SingleNoteOptionsBottomSheet`)
  - `MonospaceUtils.kt`: Span models (`TextSpan`), markdown parser, span adjustment and normalization helpers
  - `MonoTextToolbar.kt`: Custom floating text selection toolbar providing Monospace styling action
  - `ui/theme/`: Theme definition (`Theme.kt`), Google Sans typography (`Type.kt`), and color palette (`Color.kt`)
- `app/src/test/java/com/oneline/notes/`: JVM unit tests (`NoteStorageTest.kt`, `NoteFilterTest.kt`, `MonospaceUtilsTest.kt`)

## 3. Data Model
Defined in `NoteModels.kt` and `MonospaceUtils.kt`:
- **`NoteItem`** (`NoteModels.kt`): `id` (UUID String), `text` (String), `createdAt` (Long), `updatedAt` (Long?), `spans` (MutableList<TextSpan>), `type` (NoteType), `title` (String?), `fieldItems` (MutableList<FieldValueEntry>), `field` (String?), `value` (String?).
- **`FieldValueEntry`** (`NoteModels.kt`): `id` (UUID String), `field` (String), `value` (String), `insertionOrder` (Int).
- **`NoteType`** (`NoteModels.kt`): Enum with values `NORMAL` and `FIELD_VALUE`.
- **`NoteFilter`** (`NoteModels.kt`): Enum with values `ALL`, `NOTES`, and `FIELDS_VALUES`.
- **`TextSpan`** (`MonospaceUtils.kt`): `start` (Int), `end` (Int), `type` (String, default "mono").
- **Relationships**: `NoteItem` embeds a list of `TextSpan` ranges. When `type == FIELD_VALUE`, `NoteItem` contains ordered `FieldValueEntry` items.

## 4. Storage and Sync
- **Local Files**: Persisted as JSON in internal storage (`context.filesDir/one_line_notes.json`).
- **Resilience**: Per-entry parse error tolerance; corrupt files backed up to `one_line_notes.corrupt-<timestamp>.json`; atomic writes via temporary file rename (`one_line_notes.json.tmp`). Tested in `NoteStorageTest.kt`.
- **Preferences**: None. No theme preference (`pref_dark_theme` removed) or settings persisted.
- **Cloud / Database**: None. No SQLite, Room database, or remote network synchronization exists.

## 5. Features
- **Quick Single-Line Composer** (`NoteComponents.kt`, `MainActivity.kt`): Pill-shaped input with circular Send button - *Working*
- **Key-Value / Field Note Creation** (`NoteDialogs.kt`, `NoteComponents.kt`): Multi-row dialog with dynamic row delete - *Working*
- **Note Editing** (`NoteDialogs.kt`): Text note editor & field-value group editor - *Working*
- **Note Deletion & Undo** (`NoteDialogs.kt`, `NoteComponents.kt`): Unified dialog & position-restoring undo snackbar - *Working*
- **Multi-Selection Mode** (`NoteComponents.kt`, `NoteBubble.kt`): Long-press selection, composer auto-hide, bulk actions - *Working*
- **Clipboard & Partial Copy** (`NoteDialogs.kt`, `NoteBubble.kt`): Full copy, structured field-value snippet selection, tap-to-copy - *Working*
- **Search & Filter Swiping** (`NoteComponents.kt`, `MainActivity.kt`): Pill filter chips, list swipe navigation, query search - *Working*
- **Monospace Text Formatting** (`MonospaceUtils.kt`, `MonoTextToolbar.kt`): Custom selection toolbar action & spans - *Working*

## 6. State Management and Navigation Approach
- **State Management**: Local Compose state via `remember { mutableStateOf(...) }` and `mutableStateListOf<NoteItem>()` in `OneLineNotesScreen`. No ViewModel or external state library.
- **Navigation**: Single-activity architecture (`MainActivity`) without Navigation Component. Modals and bottom sheets toggled via boolean state flags (`showBottomSheet`, `showEditDialog`, `showDeleteConfirmDialog`, etc.). Back gesture handled by `BackHandler`.

## 7. Conventions
- **Naming**: PascalCase for Composables and Data Classes; camelCase for functions and variables.
- **Architecture**: Jetpack Compose with Material 3 Expressive styling (`MaterialExpressiveTheme`), using an exclusively fixed Dark Mode theme with Google Sans font.
- **UI Elements**: Pill-shaped filter chips and composer text field; circular Send and "+" buttons.
- **Data Serialization**: Manual JSON mapping (`JSONObject`, `JSONArray`) wrapped in per-entry `try-catch` blocks.
- **Feedback**: Haptic feedback (`LocalHapticFeedback`) triggered on selection, long press, filter swipe, and copy actions.

## 8. Known Bugs, TODOs, or Half-Finished Areas
- No explicit `TODO` or `FIXME` comments in codebase.
- **Main Thread File I/O**: `saveNotesToStorage` and `loadNotesFromStorage` execute synchronously on the main/UI thread instead of using `Dispatchers.IO`.
- **Reflection in Custom Toolbar**: `MonoTextToolbar.kt` accesses internal Android framework classes (`FloatingToolbar`) via reflection, which may fail on custom vendor ROMs.
- **Deprecated Context Menu Setting**: `ComposeFoundationFlags.isNewContextMenuEnabled = false` is used globally to preserve custom text toolbar hooks.
- **Storage Lifetime & Lack of Export/Import**: All notes live only in app-internal storage, so Clear data or uninstall deletes them, and no export/import exists.
- **Backup Limitations**: Corrupt-file backups are in the same folder, so they are wiped too, and nothing in the app restores from them.
