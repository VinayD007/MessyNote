# Messy Note — Project Context

Verified directly against source code and Gradle configurations as of October 2026. Reflects current implementation only.

## 1. Stack, SDK & Build Commands
- **Language & Runtime**: Kotlin 2.2.10 (Kotlin Compose plugin 2.2.10), Java 11 bytecode compatibility (`JavaVersion.VERSION_11`).
- **Framework**: Android native with Jetpack Compose & Material 3 Expressive (`androidx.compose.material3:material3:1.5.0-alpha18`).
- **Build System**: Android Gradle Plugin (AGP) 9.1.0, Gradle Wrapper 9.3.1.
- **Dependency Versions** (`gradle/libs.versions.toml`):
  - `androidx.core:core-ktx:1.10.1`
  - `androidx.lifecycle:lifecycle-runtime-ktx:2.6.1`
  - `androidx.activity:activity-compose:1.8.0`
  - `androidx.compose:compose-bom:2026.02.01` (manages `ui`, `ui-graphics`, `ui-tooling-preview`, debug `ui-tooling`)
  - `androidx.compose.material3:material3:1.5.0-alpha18`
  - `org.json:json:20231013` (testImplementation)
  - `junit:junit:4.13.2` (testImplementation)
- **SDK & Package**:
  - `namespace = "com.oneline.notes"`, `applicationId = "com.oneline.notes"`
  - `minSdk = 26`, `targetSdk = 36`, `compileSdk = release(36) { minorApiLevel = 1 }` (API 36.1)
  - `versionCode = 1`, `versionName = "1.0"`
- **Build & Test Commands** (run from project root in Windows PowerShell):
  - Build Debug APK: `.\gradlew.bat assembleDebug`
  - Install & Run on device/emulator: `.\gradlew.bat installDebug`; `adb shell am start -n com.oneline.notes/.MainActivity`
  - Run Unit Tests: `.\gradlew.bat test` (or `.\gradlew.bat testDebugUnitTest`)
  - Unit test suite: `FavoritesTest`, `MonoSelectionUnitTest`, `MonospaceSpanTest`, `NoteFilterTest`, `NoteOrderTest`, `NoteStorageTest`, `UrlDetectionTest`.

## 2. Folder Structure & Source Inventory
Single-activity architecture without external architecture layers (no ViewModel, Repository, Room, or Navigation Compose). All source resides in `app/src/main/java`:
- `MainActivity.kt`:
  - Classes: `MainActivity : ComponentActivity`, `CircularRevealClipShape : Shape` (private)
  - Composables: `OneLineNotesScreen`, `ThemeRevealLayers` (private)
  - Globals/Helpers: `LocalTransientMessageSender`, `calculateRevealRadius` (private)
- `MonoTextToolbar.kt`:
  - Classes: `MonoTextToolbar : TextToolbar` (wraps native floating `ActionMode.Callback2` with `MENU_MONO`)
- `MonospaceUtils.kt`:
  - Classes: `TextSpan` (data class: `start`, `end`, `type = "mono"`)
  - Functions: `normalizeSpans`, `toggleMonospaceSpan`, `adjustSpansForTrim`, `adjustSpansForTextChange`, `findMonospaceSpanAtOffset`, `parseMarkdownBackticks`, `buildMonospaceAnnotatedString`, `spansToJson`, `spansFromJson`
- `NoteBubble.kt`:
  - Composables: `SentNoteBubble` (universal bubble for NORMAL, FIELD_VALUE, and LIST)
  - Classes: `ListRowData` (private data class)
- `NoteComponents.kt`:
  - Composables: `MultiSelectHeader`, `StarIcon`, `ThemeModeToggle` (private), `NormalHeader`, `FavoritesHeader`, `SearchBarHeader`, `HomeFilterControl`, `SwipeableSnackbar`, `NotesComposer`, `ExpandedNoteComposer`, `NotesEmptyState`, `NotesList`
  - Classes: `SwipeableSnackbarState`
  - Helpers/Constants: `rememberSwipeableSnackbarState`, `HeaderRowHeight = 64.dp`, `ActionButtonSize = 48.dp`
- `NoteDialogs.kt`:
  - Interfaces/Adapters: `ScrollbarAdapter` (private), `ScrollStateAdapter` (private), `LazyListScrollbarAdapter` (private), `PartialCopyPairItem` (private)
  - Composables: `SingleNoteOptionsBottomSheet`, `DraggableScrollbar` (private), `PartialCopyDialog` (for NoteItem), `ScrollableSingleLineTextField` (private), `FieldValueRow` (private), `FullScreenEditPage` (private), `FieldValueGroupDialog`, `NewFieldValueGroupDialog`, `EditFieldValueGroupDialog`, `ListRow` (private), `ListGroupDialog`, `NewListGroupDialog`, `EditListGroupDialog`, `EditNoteDialog`, `DeleteConfirmDialog`, `DiscardConfirmDialog`
- `NoteModels.kt`:
  - Enums: `NoteType` (`NORMAL`, `FIELD_VALUE`, `LIST`), `NoteFilter` (`ALL`, `NOTES`, `FIELDS_VALUES`, `LISTS`)
  - Classes: `FieldValueEntry`, `ListRowEntry`, `NoteItem`
  - Functions: `NoteItem.getSortedFieldItems`, `NoteItem.getSortedListItems`, `NoteItem.buildListText`, `NoteItem.buildGroupedText`, `filterNotes`, `favoritesFor`, `orderNotesNewestFirst`, `getDisplayedNotes`, `getDisplayedFavorites`
- `NoteStorage.kt`:
  - Classes: `ParseNotesResult`
  - Functions: `parseNotesFromJson`, `backupCorruptFile` (private), `notesToJson`, `saveNotesToStorage`, `loadNotesFromStorage`, `formatTime`
- `UrlUtils.kt`:
  - Classes: `LinkSpan`, `PartialCopyWord`
  - Functions: `extractUrlSpans`, `normalizeUrlForIntent`, `buildNoteBubbleAnnotatedString`, `tokenizeForPartialCopy`, `launchUrl`
- `ui/theme/Color.kt`:
  - Classes: `MessNoteColors`
  - Palettes: `DarkMessNoteColors`, `LightMessNoteColors`, `LocalMessNoteColors`
  - Color token accessors: `BgDark`, `HeaderBg`, `SurfaceCard`, `NoteBubbleAccent`, `SurfaceDark`, `SurfaceElevated`, `PrimaryBlue`, `BrightBlue`, `DestructiveAction`, `MonospaceBg`, `MonospaceText`, `TextPrimary`, `TextSecondary`, `TextMuted`, `BorderSubtle`, `OnAccent`, `LinkAccent`
- `ui/theme/Theme.kt`:
  - Theme provider: `OneLineNotesTheme`, `LocalThemeTransitionProgress`, `ThemeTransitionMillis = 560`, `DarkColorScheme`, `LightColorScheme`
- `ui/theme/Type.kt`:
  - Typography: `GoogleSans` (`FontFamily` with weights 400, 500, 700), `AppTypography` (`Material3 Typography`)

## 3. Data Model
- **NoteType**: `NORMAL` (standard text note), `FIELD_VALUE` (structured key-value pairs), `LIST` (numbered list items).
- **FieldValueEntry**: `val id: String = UUID.randomUUID().toString()`, `var field: String`, `var value: String`, `val insertionOrder: Int = 0`.
- **ListRowEntry**: `val id: String = UUID.randomUUID().toString()`, `var value: String`, `val insertionOrder: Int = 0`.
- **NoteItem**:
  - `val id: String = UUID.randomUUID().toString()`
  - `var text: String` (required body or compiled string)
  - `val createdAt: Long = System.currentTimeMillis()`
  - `var updatedAt: Long? = null` (optional timestamp)
  - `var spans: MutableList<TextSpan> = mutableListOf()` (formatting spans)
  - `var type: NoteType = NoteType.NORMAL`
  - `var title: String? = null` (optional title for FIELD_VALUE and LIST)
  - `var fieldItems: MutableList<FieldValueEntry> = mutableListOf()`
  - `var listItems: MutableList<ListRowEntry> = mutableListOf()`
  - `var favorite: Boolean = false`
- **Backward-Compatible JSON Parsing**:
  - Missing `id` defaults to fresh `UUID.randomUUID().toString()`; missing `createdAt` defaults to current epoch millis; missing `updatedAt` parses as `null`.
  - Missing/unknown `type` string defaults to `NoteType.NORMAL`. Missing `favorite` defaults to `false`. Missing `spans` defaults to empty list.
  - Missing `title`: for `FIELD_VALUE` with items, falls back to first item's field name; otherwise `null`.
  - Child row missing `id` generates a UUID; missing child `insertionOrder` defaults to its JSON array index.
  - Legacy `LIST` note with empty `text` but populated `listItems` dynamically rebuilds its numbered text (`1. Item\n2. Item`).
  - Malformed note JSON objects are skipped individually without aborting valid sibling notes (`hadErrors = true`).

## 4. Storage & Persistence
- **File Location**: `context.filesDir/one_line_notes.json` (synchronously read/written on caller thread using `org.json`).
- **Atomic-Write Mechanism**: `saveNotesToStorage` writes full JSON to `one_line_notes.json.tmp`. It attempts `tempFile.renameTo(targetFile)`. If failed, it deletes `targetFile` and retries `tempFile.renameTo(targetFile)`.
- **Corruption Recovery**: `loadNotesFromStorage` triggers `backupCorruptFile(context, file)` whenever `hadErrors` is flagged during parse or an unhandled JSON parse exception occurs. It copies the corrupt file to `one_line_notes.corrupt-<timestamp>[-counter].json` (`overwrite = false`), preserving partial valid notes.
- **Persisted State**: All note data (`id`, `text`, `createdAt`, `updatedAt`, `spans`, `type`, `favorite`, `title`, `fieldItems`, `listItems`). Theme mode preference is persisted separately in `SharedPreferences` (`mess_note_preferences`, key `theme_mode`: `"dark"`, `"light"`, or system default if absent).
- **Non-Persisted State**: Search queries, selected filter tab, multi-selection state, plus menu visibility, dialog input drafts, scroll positions, composer draft text and spans.

## 5. Theme & Visual Tokens
Tokens defined in `Color.kt` (exact hex values):
- **Dark Mode (`DarkMessNoteColors`)**:
  - `bg`: `#000000`, `headerBg`: `#1C1C1E`, `surfaceCard`: `#1C1C1E`, `surfaceDark`: `#1C1C1E`, `surfaceElevated`: `#1C1C1E`
  - `noteBubbleBackground`: `#000000`, `noteBubbleAccent`: `#FFD60A`
  - `primaryBlue`: `#FFD60A`, `brightBlue`: `#FFD60A`, `onAccent`: `#000000`
  - `selectedItemBackground`: `#3A3310`, `destructiveAction`: `#EF5350`
  - `monospaceBg`: `#1C1C1E`, `monospaceText`: `#FFD60A`, `linkAccent`: `#FFD60A`
  - `textPrimary`: `#FFFFFF`, `textSecondary`: `#B0B0B5`, `textMuted`: `#8E8E93`, `borderSubtle`: `#38383A`
- **Light Mode (`LightMessNoteColors`)**:
  - `bg`: `#FFFFFF`, `headerBg`: `#F2F2F7`, `surfaceCard`: `#F2F2F7`, `surfaceDark`: `#F2F2F7`, `surfaceElevated`: `#F2F2F7`
  - `noteBubbleBackground`: `#FFFFFF`, `noteBubbleAccent`: `#FFCC00`
  - `primaryBlue`: `#FFCC00`, `brightBlue`: `#FFCC00`, `onAccent`: `#000000`
  - `selectedItemBackground`: `#FFF2B2`, `destructiveAction`: `#C62828`
  - `monospaceBg`: `#F2F2F7`, `monospaceText`: `#6B5200`, `linkAccent`: `#6B5200`
  - `textPrimary`: `#000000`, `textSecondary`: `#5C5C60`, `textMuted`: `#76767A`, `borderSubtle`: `#D1D1D6`
- **Typography & Font**: `GoogleSans` family (`google_sans_regular.ttf` [W400], `google_sans_medium.ttf` [W500], `google_sans_bold.ttf` [W700]). Monospace styled spans use `FontFamily.Monospace`. Explicit sizes: 11sp (meta), 13sp (chips), 14sp (inputs/body), 15sp (buttons/items), 16sp (subtitles), 17sp (count), 18sp (headers/empty), 20sp (sheet title), 24sp (dialog title).
- **Material 3 Expressive APIs**: `MaterialExpressiveTheme` in `Theme.kt`; `ToggleButton`, `ToggleButtonDefaults`, `ButtonGroupDefaults.connectedLeadingButtonShapes()`, `connectedMiddleButtonShapes()`, `connectedTrailingButtonShapes()`, and `ConnectedSpaceBetween` in `HomeFilterControl`.

## 6. Implemented Features Overview
1. **Dynamic Theme & Circular Reveal**: Toggle in header triggers dual-layer circular reveal animation expanding from measured button origin (350ms `FastOutSlowInEasing`). System bars toggle light/dark icons at 50% progress.
2. **Reverse Chronological Feed**: Sorted newest-first by `createdAt` (equal timestamps ordered by later storage index first). Stable keys `type:id:occurrence`.
3. **Filter Navigation**: 4 filter segments (`All`, `Notes`, `Fields-Values`, `Lists`). List supports horizontal swipe gesture (64dp displacement) to shift adjacent filters.
4. **Live Search**: Case-insensitive substring matching on body, titles, field names, field values, and list items. Separate search states for Home and Favorites.
5. **Favorites System**: Star toggle per note. Dedicated Favorites screen with search and single-step snackbar Undo on removal.
6. **Monospace & Fast Copy**: Markdown backticks or selection toolbar "Mono" action format text. Tapping monospace snippet in bubble immediately copies it to clipboard.
7. **URL Detection & Safe Open**: `PatternsCompat.WEB_URL` detects web addresses, stripping trailing punctuation. Tap opens modal "Open link?" confirmation before dispatching `ACTION_VIEW`.
8. **Single Note Options Bottom Sheet**: Modal sheet with Favorite, Copy, Copy partially, Edit, and Delete actions.
9. **Partial Copy**: Interactive dialog with selectable tokens (words for normal notes, field/value tokens, numbered rows for lists). Supports single-tap, 200ms long-press drag range selection, vertical auto-scroll, and draggable scrollbar.
10. **Collapsible Composer & Expansion**: Multiline input up to 4 lines with glassmorphism blur (Android 12+). When text exceeds 1 line, expand button opens `ExpandedNoteComposer` full-screen editor.
11. **Structured Note Dialogs**: Full-screen dialogs for Field-Value notes (dynamic key-value pairs) and List notes (dynamic numbered items) with discard confirmation guards.
12. **Multi-Selection Mode**: Triggered by long-pressing any note. Header offers Edit (1 note), Copy Partially (1 note), Copy (all), Delete (all).

## 7. Known Bugs, Risks & Deprecations
- **Main Thread I/O**: `saveNotesToStorage` and `loadNotesFromStorage` execute synchronously on the main UI thread. Large datasets can produce frame jank.
- **Deprecated APIs in Use**:
  - `ClickableText` in `NoteBubble.kt:189, 313, 329` (deprecated in Compose Foundation, replaced by `Text` with link annotations).
  - `WindowCompat.getInsetsController` & `window.statusBarColor` / `navigationBarColor` in `Theme.kt` and `MainActivity.kt` (deprecated in Android 15 edge-to-edge).
- **Experimental Opt-Ins**: `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`, `@OptIn(ExperimentalFoundationApi::class)` (`ComposeFoundationFlags.isNewContextMenuEnabled = false`), `@OptIn(ExperimentalLayoutApi::class)` (`FlowRow`), `@OptIn(ExperimentalMaterial3Api::class)`.
- **Silent Exception Handling**: Corrupt backup failures swallow exceptions (`catch (_: Exception) {}`). Storage save catches Exception and only prints stack traces.
- **Force-Unwrapped Assertions**: `linkToOpen!!` in `NoteBubble.kt:366` and `activeNote!!` in `MainActivity.kt` (guarded by enclosing null-checks, but risk NPE if race conditions occur).

## 8. Unused / Dead Code Audit
Summary of dead-code removals and verified dependencies:
- **Cleaned Up**: Removed unused `isSelected` parameter and unnested outer `Box` in `SentNoteBubble` ([NoteBubble.kt](file:///c:/Users/Lenovo/.gemini/antigravity-ide/scratch/one-line-notes-android/app/src/main/java/com/oneline/notes/NoteBubble.kt)); removed dead `onNewFieldClick` parameter from `NotesComposer` ([NoteComponents.kt](file:///c:/Users/Lenovo/.gemini/antigravity-ide/scratch/one-line-notes-android/app/src/main/java/com/oneline/notes/NoteComponents.kt)) and its call site ([MainActivity.kt](file:///c:/Users/Lenovo/.gemini/antigravity-ide/scratch/one-line-notes-android/app/src/main/java/com/oneline/notes/MainActivity.kt)); removed uncalled String-based `PartialCopyDialog` overload ([NoteDialogs.kt](file:///c:/Users/Lenovo/.gemini/antigravity-ide/scratch/one-line-notes-android/app/src/main/java/com/oneline/notes/NoteDialogs.kt)); removed 8 unused color token getters (`NoteBubbleBackground`, `SentBubbleColor`, `PrimaryIndigo`, `ActiveAccent`, `AccentCyan`, `SecondaryViolet`, `SelectedItemBackground`, `AccentDanger`) from [Color.kt](file:///c:/Users/Lenovo/.gemini/antigravity-ide/scratch/one-line-notes-android/app/src/main/java/com/oneline/notes/ui/theme/Color.kt).
- **Retained Dependency**: `findMonospaceSpanAtOffset` in `MonospaceUtils.kt:159-162` is retained because it is actively called by 20 unit tests across `MonospaceSpanTest.kt` and `MonoSelectionUnitTest.kt`. Deleting it would break unit test compilation.
- **Removed Note Truncation / Collapse Feature**: All variables `isExpandableNote`, `isExpanded`, `isLongNote`, 4-line note truncation, and circular ring button canvas were completely excised from `NoteBubble.kt`. Composer expansion `isComposerExpanded`/`ExpandedNoteComposer` is an active drafting feature, not note-bubble truncation.
