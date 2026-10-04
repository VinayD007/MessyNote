package com.oneline.notes

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.oneline.notes.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import kotlin.math.sqrt

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComposeFoundationFlags.isNewContextMenuEnabled = false
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themePreferences = remember {
                context.getSharedPreferences("mess_note_preferences", MODE_PRIVATE)
            }
            var themeOverride by rememberSaveable {
                mutableStateOf(themePreferences.getString("theme_mode", null))
            }
            val systemDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = when (themeOverride) {
                "dark" -> true
                "light" -> false
                else -> systemDarkTheme
            }

            OneLineNotesScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = {
                    val nextTheme = if (isDarkTheme) "light" else "dark"
                    themeOverride = nextTheme
                    themePreferences.edit().putString("theme_mode", nextTheme).apply()
                }
            )
        }
    }
}

val LocalTransientMessageSender = staticCompositionLocalOf<(String) -> Unit> { {} }

@Suppress("DEPRECATION")
@Composable
fun OneLineNotesScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var committedDarkTheme by remember { mutableStateOf(isDarkTheme) }
    var transitionTarget by remember { mutableStateOf<Boolean?>(null) }
    val revealAnimation = remember(transitionTarget) { Animatable(0f) }
    val revealProgress = remember(revealAnimation) { derivedStateOf { revealAnimation.value } }

    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val showTransientMessage: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }
    val listState = rememberLazyListState()
    val revealListState = remember(transitionTarget) {
        LazyListState(
            firstVisibleItemIndex = listState.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
        )
    }
    val filterScrollState = rememberScrollState()
    val revealFilterScrollState = remember(transitionTarget) {
        ScrollState(initial = filterScrollState.value)
    }
    val swipeableSnackbarState = rememberSwipeableSnackbarState()

    // Notes State
    val notes = remember { mutableStateListOf<NoteItem>() }
    var inputTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var inputSpans by remember { mutableStateOf(listOf<TextSpan>()) }
    var isComposerExpanded by rememberSaveable { mutableStateOf(false) }
    var composerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val composerHeightDp = with(density) { composerHeightPx.toDp() }
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val composerReservedHeight = composerHeightDp + navigationBarHeight + keyboardHeight

    // Search State
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    // Multi-Selection State
    val selectedNoteIds = remember { mutableStateListOf<String>() }
    val isMultiSelectMode = selectedNoteIds.isNotEmpty()

    // Favorites Page & Search States
    var isFavoritesPage by rememberSaveable { mutableStateOf(false) }
    var isFavSearchActive by rememberSaveable { mutableStateOf(false) }
    var favSearchQuery by rememberSaveable { mutableStateOf("") }
    val favSearchFocusRequester = remember { FocusRequester() }
    val favListState = rememberLazyListState()

    // Clear focus and hide keyboard when selection mode starts
    LaunchedEffect(isMultiSelectMode) {
        if (isMultiSelectMode) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    // Clear selection and auto-focus favorites search when opened
    LaunchedEffect(isFavSearchActive) {
        selectedNoteIds.clear()
        if (isFavSearchActive) {
            favSearchFocusRequester.requestFocus()
        }
    }

    // Open favorites scrolled to top (newest first); return to home scrolled to top
    LaunchedEffect(isFavoritesPage) {
        if (isFavoritesPage) {
            favListState.scrollToItem(0)
        } else {
            if (notes.isNotEmpty()) {
                listState.scrollToItem(0)
            }
        }
    }

    // Dialog & Sheet States
    var activeNote by remember { mutableStateOf<NoteItem?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showPartialCopyDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showNewFieldDialog by remember { mutableStateOf(false) }
    var showEditFieldDialog by remember { mutableStateOf(false) }
    var showNewListDialog by remember { mutableStateOf(false) }
    var showEditListDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isPlusMenuExpanded by remember { mutableStateOf(false) }
    var notesToDelete by remember { mutableStateOf<List<NoteItem>>(emptyList()) }

    // Handle system back gesture in prioritized order:
    // expanded composer -> selection mode -> search -> dialogs/popup -> Favorites page -> home
    val anyDialogShowing = showBottomSheet ||
        showPartialCopyDialog ||
        showEditDialog ||
        showNewFieldDialog ||
        showEditFieldDialog ||
        showNewListDialog ||
        showEditListDialog ||
        showDeleteConfirmDialog
    val isEditPageShowing = showEditDialog || showEditFieldDialog || showEditListDialog
    val activeSearch = if (isFavoritesPage) isFavSearchActive else isSearchActive
    val canStartThemeReveal = !anyDialogShowing &&
        !isPlusMenuExpanded &&
        !isFavoritesPage &&
        !isSearchActive &&
        !isComposerExpanded &&
        !isMultiSelectMode
    val isThemeTransitioning = transitionTarget != null

    val toggleProgress = remember(committedDarkTheme, transitionTarget, revealProgress) {
        derivedStateOf {
            val start = if (committedDarkTheme) 0f else 1f
            val target = transitionTarget ?: committedDarkTheme
            val end = if (target) 0f else 1f
            if (transitionTarget == null) start else start + ((end - start) * revealProgress.value)
        }
    }

    LaunchedEffect(isDarkTheme, committedDarkTheme, transitionTarget, canStartThemeReveal) {
        if (transitionTarget == null && canStartThemeReveal && isDarkTheme != committedDarkTheme) {
            transitionTarget = isDarkTheme
        }
    }

    LaunchedEffect(transitionTarget, revealAnimation) {
        val target = transitionTarget ?: return@LaunchedEffect
        revealAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 350,
                easing = FastOutSlowInEasing
            )
        )
        Snapshot.withMutableSnapshot {
            committedDarkTheme = target
            transitionTarget = null
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    LaunchedEffect(transitionTarget, committedDarkTheme, revealProgress) {
        val iconTheme = transitionTarget
        if (iconTheme != null) {
            snapshotFlow { revealProgress.value }.first { it >= 0.5f }
        }
        if (!view.isInEditMode) {
            val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !(iconTheme ?: committedDarkTheme)
            controller.isAppearanceLightNavigationBars = !(iconTheme ?: committedDarkTheme)
        }
    }

    val requestThemeToggle = {
        if (!isThemeTransitioning && canStartThemeReveal && isDarkTheme == committedDarkTheme) {
            transitionTarget = !committedDarkTheme
            onThemeToggle()
        }
    }

    BackHandler(enabled = !isThemeTransitioning && !isEditPageShowing && (isComposerExpanded || isPlusMenuExpanded || isMultiSelectMode || activeSearch || anyDialogShowing || isFavoritesPage)) {
        when {
            isComposerExpanded -> {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                isComposerExpanded = false
            }
            isPlusMenuExpanded -> {
                isPlusMenuExpanded = false
            }
            isMultiSelectMode -> {
                selectedNoteIds.clear()
            }
            activeSearch -> {
                if (isFavoritesPage) {
                    favSearchQuery = ""
                    isFavSearchActive = false
                } else {
                    searchQuery = ""
                    isSearchActive = false
                }
            }
            anyDialogShowing -> {
                showBottomSheet = false
                showPartialCopyDialog = false
                showDeleteConfirmDialog = false
            }
            isFavoritesPage -> {
                selectedNoteIds.clear()
                favSearchQuery = ""
                isFavSearchActive = false
                isFavoritesPage = false
            }
        }
    }

    // Home Content Filter State
    var selectedFilter by remember { mutableStateOf(NoteFilter.ALL) }

    // Clear selection whenever filter changes or search is opened/closed
    LaunchedEffect(selectedFilter) {
        selectedNoteIds.clear()
        isPlusMenuExpanded = false
    }

    LaunchedEffect(selectedFilter, searchQuery) {
        listState.scrollToItem(0)
    }

    // Filtered Notes based on active filter and search query (derivedStateOf ensures immediate updates on item mutation)
    val displayedNotes by remember {
        derivedStateOf {
            getDisplayedNotes(notes, selectedFilter, searchQuery)
        }
    }

    val displayedFavNotes by remember {
        derivedStateOf {
            getDisplayedFavorites(notes, favSearchQuery)
        }
    }

    // Load initial notes from storage (no default welcome note), open scrolled to top
    LaunchedEffect(Unit) {
        val loaded = loadNotesFromStorage(context)
        val cleaned = loaded.filterNot {
            it.text.contains("Welcome to OneLine Notes", ignoreCase = true) ||
            it.text.contains("Welcome to MessNote", ignoreCase = true) ||
            it.text.contains("Welcome to Messy Note", ignoreCase = true)
        }
        notes.clear()
        notes.addAll(cleaned)
        if (loaded.size != cleaned.size) {
            saveNotesToStorage(context, cleaned)
        }
        if (notes.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    // Clear selection and auto-focus search when opened/closed
    LaunchedEffect(isSearchActive) {
        selectedNoteIds.clear()
        isPlusMenuExpanded = false
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }

    // Function to add a note
    val onSendNote = {
        val rawText = inputTextFieldValue.text
        val trimmed = rawText.trim()
        if (trimmed.isNotEmpty()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val (cleanText, markdownSpans) = parseMarkdownBackticks(trimmed)
            // Adjust any composer-applied spans if leading/trailing whitespace was trimmed
            val adjustedComposerSpans = adjustSpansForTextChange(rawText, trimmed, inputSpans)
            val mergedSpans = normalizeSpans(cleanText.length, adjustedComposerSpans + markdownSpans)
            val newNote = NoteItem(
                text = cleanText,
                spans = mergedSpans.toMutableList()
            )
            notes.add(newNote)
            saveNotesToStorage(context, notes)
            inputTextFieldValue = TextFieldValue("")
            inputSpans = emptyList()
            // Clear search when sending a new note so user sees it
            if (searchQuery.isNotBlank()) {
                searchQuery = ""
                isSearchActive = false
            }
            coroutineScope.launch {
                if (displayedNotes.isNotEmpty()) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    val renderAppUi: @Composable (Boolean, Boolean, Boolean, (Offset) -> Unit) -> Unit = {
            renderedDarkTheme,
            isRevealLayer,
            themeToggleEnabled,
            onThemeTogglePositioned ->
        CompositionLocalProvider(LocalTransientMessageSender provides showTransientMessage) {
        val screenSemantics = if (isThemeTransitioning) Modifier.clearAndSetSemantics {} else Modifier
        Box(modifier = Modifier.fillMaxSize().then(screenSemantics)) {
        val backdropLayer = rememberGraphicsLayer()
        val blurredBackdropLayer = rememberGraphicsLayer()
        var scaffoldOriginInRoot by remember { mutableStateOf(Offset.Zero) }
        var composerBlurBounds by remember { mutableStateOf<Rect?>(null) }
        val showComposer = !isComposerExpanded && !isFavoritesPage && !isSearchActive && !isMultiSelectMode
        val density = LocalDensity.current
        val blurRadiusPx = with(density) { 14.dp.toPx() }
        val panelCornerRadiusPx = with(density) { 22.dp.toPx() }

        SideEffect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                blurredBackdropLayer.renderEffect = BlurEffect(
                    radiusX = blurRadiusPx,
                    radiusY = blurRadiusPx,
                    edgeTreatment = TileMode.Clamp
                )
            }
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .onGloballyPositioned { coordinates ->
                    val origin = coordinates.positionInRoot()
                    if (scaffoldOriginInRoot != origin) scaffoldOriginInRoot = origin
                }
                .drawWithContent {
                    backdropLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(backdropLayer)
                },
            containerColor = BgDark,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {},
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HeaderBg
                ) {
                    if (isMultiSelectMode) {
                        MultiSelectHeader(
                            selectedCount = selectedNoteIds.size,
                            onClose = { selectedNoteIds.clear() },
                            onEdit = {
                                val targetId = selectedNoteIds.firstOrNull()
                                val targetNote = notes.firstOrNull { it.id == targetId }
                                if (targetNote != null) {
                                    activeNote = targetNote
                                    if (targetNote.type == NoteType.FIELD_VALUE) {
                                        showEditFieldDialog = true
                                    } else if (targetNote.type == NoteType.LIST) {
                                        showEditListDialog = true
                                    } else {
                                        showEditDialog = true
                                    }
                                    selectedNoteIds.clear()
                                }
                            },
                            onCopy = {
                                val selectedNotes = notes.filter { selectedNoteIds.contains(it.id) }
                                if (selectedNotes.isNotEmpty()) {
                                    val combinedText = selectedNotes.joinToString("\n\n") { it.buildGroupedText() }
                                    clipboardManager.setText(AnnotatedString(combinedText))
                                    val msg = "${selectedNotes.size} ${if (selectedNotes.size == 1) "note" else "notes"} copied!"
                                    showTransientMessage(msg)
                                    selectedNoteIds.clear()
                                }
                            },
                            onCopyPartially = {
                                val targetId = selectedNoteIds.firstOrNull()
                                val targetNote = notes.firstOrNull { it.id == targetId }
                                if (targetNote != null) {
                                    activeNote = targetNote
                                    showPartialCopyDialog = true
                                    selectedNoteIds.clear()
                                }
                            },
                            onDelete = {
                                val toDelete = notes.filter { selectedNoteIds.contains(it.id) }
                                if (toDelete.isNotEmpty()) {
                                    notesToDelete = toDelete
                                    showDeleteConfirmDialog = true
                                }
                            }
                        )
                    } else if (isFavoritesPage) {
                        if (isFavSearchActive) {
                            SearchBarHeader(
                                searchQuery = favSearchQuery,
                                onSearchQueryChange = { favSearchQuery = it },
                                searchFocusRequester = favSearchFocusRequester,
                                onExitSearch = {
                                    favSearchQuery = ""
                                    isFavSearchActive = false
                                },
                                onSearchKeyboardDone = {
                                    keyboardController?.hide()
                                },
                                placeholder = "Search favorites..."
                            )
                        } else {
                            FavoritesHeader(
                                onBack = {
                                    selectedNoteIds.clear()
                                    favSearchQuery = ""
                                    isFavSearchActive = false
                                    isFavoritesPage = false
                                },
                                onSearchTrigger = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isFavSearchActive = true
                                }
                            )
                        }
                    } else if (!isSearchActive) {
                        NormalHeader(
                            isDarkTheme = renderedDarkTheme,
                            onThemeToggle = requestThemeToggle,
                            themeToggleEnabled = themeToggleEnabled && !isPlusMenuExpanded,
                            onThemeTogglePositioned = onThemeTogglePositioned,
                            onSearchTrigger = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                isSearchActive = true
                            },
                            onFavoritesClick = {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                isFavoritesPage = true
                            }
                        )
                    } else {
                        SearchBarHeader(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            searchFocusRequester = searchFocusRequester,
                            onExitSearch = {
                                searchQuery = ""
                                isSearchActive = false
                            },
                            onSearchKeyboardDone = {
                                keyboardController?.hide()
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val onFilterSelected: (NoteFilter) -> Unit = { filter ->
                if (selectedFilter != filter) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedFilter = filter
                }
            }
            val currentFilter by rememberUpdatedState(selectedFilter)
            val currentOnFilterSelected by rememberUpdatedState(onFilterSelected)
            val swipeModifier = if (!isMultiSelectMode) {
                Modifier.pointerInput(Unit) {
                    val touchSlop = viewConfiguration.touchSlop
                    val thresholdPx = 64.dp.toPx()

                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                        val downPos = down.position
                        val pointerId = down.id
                        var isHorizontalSwipe = false
                        var hasFired = false

                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                            if (!change.pressed) {
                                if (isHorizontalSwipe) {
                                    change.consume()
                                }
                                break
                            }

                            val dx = change.position.x - downPos.x
                            val dy = change.position.y - downPos.y
                            val absDx = kotlin.math.abs(dx)
                            val absDy = kotlin.math.abs(dy)

                            if (!isHorizontalSwipe) {
                                if (absDy > touchSlop && absDx <= absDy * 1.5f) {
                                    // Vertical or diagonal list scrolling: yield to list scroll
                                    do {
                                        val ev = awaitPointerEvent(pass = PointerEventPass.Final)
                                    } while (ev.changes.any { it.pressed })
                                    break
                                }
                                if (absDx > touchSlop && absDx > absDy * 1.5f) {
                                    isHorizontalSwipe = true
                                    change.consume()
                                }
                            } else {
                                change.consume()
                                if (!hasFired) {
                                    if (dx <= -thresholdPx) {
                                        hasFired = true
                                        val next = when (currentFilter) {
                                            NoteFilter.ALL -> NoteFilter.NOTES
                                            NoteFilter.NOTES -> NoteFilter.FIELDS_VALUES
                                            NoteFilter.FIELDS_VALUES -> NoteFilter.LISTS
                                            NoteFilter.LISTS -> null
                                        }
                                        if (next != null) {
                                            currentOnFilterSelected(next)
                                        }
                                    } else if (dx >= thresholdPx) {
                                        hasFired = true
                                        val prev = when (currentFilter) {
                                            NoteFilter.LISTS -> NoteFilter.FIELDS_VALUES
                                            NoteFilter.FIELDS_VALUES -> NoteFilter.NOTES
                                            NoteFilter.NOTES -> NoteFilter.ALL
                                            NoteFilter.ALL -> null
                                        }
                                        if (prev != null) {
                                            currentOnFilterSelected(prev)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Modifier
            }

            val handleNoteClick: (NoteItem) -> Unit = { note ->
                if (isMultiSelectMode) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (selectedNoteIds.contains(note.id)) {
                        selectedNoteIds.remove(note.id)
                    } else {
                        selectedNoteIds.add(note.id)
                    }
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    activeNote = note
                    showBottomSheet = true
                }
            }

            val handleNoteLongClick: (NoteItem) -> Unit = { note ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (selectedNoteIds.contains(note.id)) {
                    selectedNoteIds.remove(note.id)
                } else {
                    if (selectedNoteIds.isEmpty()) {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    }
                    selectedNoteIds.add(note.id)
                }
            }

            val handleMonoTap: (String) -> Unit = { snippet ->
                clipboardManager.setText(AnnotatedString(snippet))
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showTransientMessage("Copied: $snippet")
            }

            if (!isFavoritesPage) {
                // Home Content Filter Control: [ All ] [ Notes ] [ Fields-Values ]
                HomeFilterControl(
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                    scrollState = if (isRevealLayer) revealFilterScrollState else filterScrollState,
                    isRevealLayer = isRevealLayer
                )

                NotesList(
                    displayedNotes = displayedNotes,
                    listState = if (isRevealLayer) revealListState else listState,
                    selectedNoteIds = selectedNoteIds,
                    isMultiSelectMode = isMultiSelectMode,
                    isSearchActive = isSearchActive,
                    hasBottomFab = !isSearchActive && !isMultiSelectMode,
                    bottomOverlayHeight = if (!isSearchActive && !isMultiSelectMode) {
                        composerReservedHeight + 8.dp
                    } else {
                        0.dp
                    },
                    onNoteClick = handleNoteClick,
                    onNoteLongClick = handleNoteLongClick,
                    onMonoTap = handleMonoTap,
                    emptyState = {
                        NotesEmptyState(
                            isSearchActive = isSearchActive,
                            searchQuery = searchQuery,
                            selectedFilter = selectedFilter
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(swipeModifier),
                    fabOverlay = {
                        if (!isSearchActive && !isMultiSelectMode) {
                            if (isPlusMenuExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            isPlusMenuExpanded = false
                                        }
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = composerReservedHeight + 8.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = isPlusMenuExpanded,
                                    enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                                    exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // "Field-Value" pill
                                        Surface(
                                            shape = CircleShape,
                                            color = PrimaryBlue,
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    isPlusMenuExpanded = false
                                                    showNewFieldDialog = true
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_edit),
                                                    contentDescription = "New Field Note",
                                                    tint = OnAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Field-Value",
                                                    color = OnAccent,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        // "List" pill
                                        Surface(
                                            shape = CircleShape,
                                            color = PrimaryBlue,
                                            shadowElevation = 4.dp,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    isPlusMenuExpanded = false
                                                    showNewListDialog = true
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_check),
                                                    contentDescription = "New List",
                                                    tint = OnAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "List",
                                                    color = OnAccent,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                val fabShape = CircleShape
                                Surface(
                                    shape = fabShape,
                                    color = PrimaryBlue,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .size(ActionButtonSize)
                                        .clip(fabShape)
                                        .clickable {
                                            isPlusMenuExpanded = !isPlusMenuExpanded
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(
                                                if (isPlusMenuExpanded) R.drawable.ic_close else R.drawable.ic_add
                                            ),
                                            contentDescription = if (isPlusMenuExpanded) "Close menu" else "Add note",
                                            tint = OnAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            } else {
                // Favorites Page List (reusing NotesList without filter chips, without filter swipe, without "+")
                NotesList(
                    displayedNotes = displayedFavNotes,
                    listState = favListState,
                    selectedNoteIds = selectedNoteIds,
                    isMultiSelectMode = isMultiSelectMode,
                    isSearchActive = isFavSearchActive,
                    hasBottomFab = false,
                    onNoteClick = handleNoteClick,
                    onNoteLongClick = handleNoteLongClick,
                    onMonoTap = handleMonoTap,
                    emptyState = {
                        NotesEmptyState(
                            isSearchActive = isFavSearchActive,
                            searchQuery = favSearchQuery,
                            selectedFilter = NoteFilter.ALL,
                            isFavorites = true
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }

        if (showComposer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && composerBlurBounds != null) {
            Canvas(modifier = Modifier.matchParentSize().zIndex(10f)) {
                val panel = composerBlurBounds ?: return@Canvas
                val localPanel = Rect(
                    left = panel.left,
                    top = panel.top,
                    right = panel.right,
                    bottom = panel.bottom
                )
                val panelPath = Path().apply {
                    addRoundRect(RoundRect(localPanel, CornerRadius(panelCornerRadiusPx)))
                }
                blurredBackdropLayer.record {
                    withTransform({
                        translate(scaffoldOriginInRoot.x, scaffoldOriginInRoot.y)
                    }) {
                        drawLayer(backdropLayer)
                    }
                }
                clipPath(panelPath) {
                    drawLayer(blurredBackdropLayer)
                }
            }
        }

        if (showComposer) {
            NotesComposer(
                inputTextFieldValue = inputTextFieldValue,
                onInputValueChange = { inputTextFieldValue = it },
                inputSpans = inputSpans,
                onSpansChange = { inputSpans = it },
                onSendNote = onSendNote,
                onExpand = {
                    keyboardController?.hide()
                    isComposerExpanded = true
                },
                onPanelBoundsChanged = { bounds -> composerBlurBounds = bounds },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .navigationBarsPadding()
                    .zIndex(20f)
                    .onSizeChanged {
                        if (!isRevealLayer) composerHeightPx = it.height
                    }
            )
        }

        // Floating Snackbar Host Overlay
        val isComposerAndFabVisible = !isFavoritesPage && !isSearchActive && !isMultiSelectMode
        val snackbarBottomPadding = if (isComposerAndFabVisible) {
            (if (composerHeightPx > 0) composerHeightDp else 62.dp) + 64.dp
        } else {
            8.dp
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(100f)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = snackbarBottomPadding)
        ) { snackbarData ->
            SwipeableSnackbar(
                snackbarData = snackbarData,
                onDismiss = { snackbarData.dismiss() },
                state = swipeableSnackbarState
            )
        }
    }

    // =========================================================================
    // Single Note Options Bottom Sheet (Copy, Copy partially, Edit, Delete, Favorite)
    // =========================================================================
    if (showBottomSheet && activeNote != null) {
        val note = activeNote!!
        SingleNoteOptionsBottomSheet(
            onDismiss = { showBottomSheet = false },
            isFavorite = note.favorite,
            onToggleFavorite = {
                val index = notes.indexOfFirst { it.id == note.id }
                val newFav = !note.favorite
                val updatedNote = note.copy(favorite = newFav)
                if (index != -1) {
                    notes[index] = updatedNote
                }
                activeNote = updatedNote
                saveNotesToStorage(context, notes)
                showBottomSheet = false

                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    if (isFavoritesPage && !newFav) {
                        val result = snackbarHostState.showSnackbar(
                            message = "Removed from favorites",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            val rIndex = notes.indexOfFirst { it.id == updatedNote.id }
                            if (rIndex != -1) {
                                val restoredNote = notes[rIndex].copy(favorite = true)
                                notes[rIndex] = restoredNote
                                saveNotesToStorage(context, notes)
                            }
                        }
                    } else {
                        val msg = if (newFav) "Added to favorites" else "Removed from favorites"
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            },
            onCopy = {
                clipboardManager.setText(AnnotatedString(note.buildGroupedText()))
                showBottomSheet = false
                showTransientMessage("Copied to clipboard!")
            },
            onCopyPartially = {
                showBottomSheet = false
                showPartialCopyDialog = true
            },
            onEdit = {
                showBottomSheet = false
                if (note.type == NoteType.FIELD_VALUE) {
                    showEditFieldDialog = true
                } else if (note.type == NoteType.LIST) {
                    showEditListDialog = true
                } else {
                    showEditDialog = true
                }
            },
            onDelete = {
                showBottomSheet = false
                notesToDelete = listOf(note)
                showDeleteConfirmDialog = true
            }
        )
    }

    // =========================================================================
    // Partial Copy Interactive Dialog
    // =========================================================================
    if (showPartialCopyDialog && activeNote != null) {
        val note = activeNote!!
        PartialCopyDialog(
            note = note,
            onDismiss = { showPartialCopyDialog = false },
            onCopySnippet = { snippet ->
                clipboardManager.setText(AnnotatedString(snippet))
                showPartialCopyDialog = false
                showTransientMessage("Snippet copied to clipboard!")
            }
        )
    }

    // =========================================================================
    // Edit Note Dialog
    // =========================================================================
    if (showEditDialog && activeNote != null) {
        val note = activeNote!!
        EditNoteDialog(
            note = note,
            onDismiss = { showEditDialog = false },
            onSave = { trimmed, spans ->
                val index = notes.indexOfFirst { it.id == note.id }
                val updatedNote = note.copy(
                    text = trimmed,
                    spans = normalizeSpans(trimmed.length, spans).toMutableList(),
                    updatedAt = System.currentTimeMillis()
                )
                if (index != -1) {
                    notes[index] = updatedNote
                }
                activeNote = updatedNote
                saveNotesToStorage(context, notes)
                showEditDialog = false
                showTransientMessage("Note updated!")
            }
        )
    }

    // =========================================================================
    // New Field/Value Dialog
    // =========================================================================
    if (showNewFieldDialog) {
        NewFieldValueGroupDialog(
            onDismiss = { showNewFieldDialog = false },
            onSave = { title, items ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val newNote = NoteItem(
                    text = "",
                    type = NoteType.FIELD_VALUE,
                    title = title,
                    fieldItems = items.toMutableList()
                ).apply {
                    text = buildGroupedText()
                }
                notes.add(newNote)
                saveNotesToStorage(context, notes)
                coroutineScope.launch {
                    if (displayedNotes.isNotEmpty()) {
                        listState.animateScrollToItem(0)
                    }
                }
                showNewFieldDialog = false
                showTransientMessage("Field note saved!")
            }
        )
    }

    // =========================================================================
    // Edit Field/Value Dialog
    // =========================================================================
    if (showEditFieldDialog && activeNote != null) {
        val note = activeNote!!
        EditFieldValueGroupDialog(
            initialTitle = note.title ?: "",
            initialItems = note.getSortedFieldItems(),
            onDismiss = { showEditFieldDialog = false },
            onSave = { updatedTitle, updatedItems ->
                val index = notes.indexOfFirst { it.id == note.id }
                val updatedNote = note.copy(
                    text = "",
                    title = updatedTitle,
                    fieldItems = updatedItems.toMutableList(),
                    updatedAt = System.currentTimeMillis()
                ).apply {
                    text = buildGroupedText()
                }
                if (index != -1) {
                    notes[index] = updatedNote
                }
                activeNote = updatedNote
                saveNotesToStorage(context, notes)
                showEditFieldDialog = false
                showTransientMessage("Note updated!")
            }
        )
    }

    // =========================================================================
    // New List Dialog
    // =========================================================================
    if (showNewListDialog) {
        NewListGroupDialog(
            onDismiss = { showNewListDialog = false },
            onSave = { title, items ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val newNote = NoteItem(
                    text = "",
                    type = NoteType.LIST,
                    title = title,
                    listItems = items.toMutableList()
                ).apply {
                    text = buildListText()
                }
                notes.add(newNote)
                saveNotesToStorage(context, notes)
                coroutineScope.launch {
                    if (displayedNotes.isNotEmpty()) {
                        listState.animateScrollToItem(0)
                    }
                }
                showNewListDialog = false
                showTransientMessage("List saved!")
            }
        )
    }

    // =========================================================================
    // Edit List Dialog
    // =========================================================================
    if (showEditListDialog && activeNote != null) {
        val note = activeNote!!
        EditListGroupDialog(
            initialTitle = note.title ?: "",
            initialItems = note.getSortedListItems(),
            onDismiss = { showEditListDialog = false },
            onSave = { updatedTitle, updatedItems ->
                val index = notes.indexOfFirst { it.id == note.id }
                val updatedNote = note.copy(
                    text = "",
                    title = updatedTitle,
                    listItems = updatedItems.toMutableList(),
                    updatedAt = System.currentTimeMillis()
                ).apply {
                    text = buildListText()
                }
                if (index != -1) {
                    notes[index] = updatedNote
                }
                activeNote = updatedNote
                saveNotesToStorage(context, notes)
                showEditListDialog = false
                showTransientMessage("List updated!")
            }
        )
    }

    // =========================================================================
    // Delete Confirmation Dialog (Unified for Single & Multi-Selection)
    // =========================================================================
    if (showDeleteConfirmDialog && notesToDelete.isNotEmpty()) {
        DeleteConfirmDialog(
            count = notesToDelete.size,
            onDismiss = {
                showDeleteConfirmDialog = false
                notesToDelete = emptyList()
            },
            onConfirm = {
                val toDelete = notesToDelete.toList()
                val indexedToDelete = toDelete.map { note ->
                    Pair(notes.indexOf(note), note)
                }.filter { it.first != -1 }

                notes.removeAll(toDelete.toSet())
                saveNotesToStorage(context, notes)
                showDeleteConfirmDialog = false
                notesToDelete = emptyList()
                selectedNoteIds.clear()

                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val message = if (indexedToDelete.size == 1) "Note deleted" else "${indexedToDelete.size} notes deleted"
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        val sortedToRestore = indexedToDelete.sortedBy { it.first }
                        for ((origIdx, n) in sortedToRestore) {
                            val insertAt = origIdx.coerceIn(0, notes.size)
                            notes.add(insertAt, n)
                        }
                        saveNotesToStorage(context, notes)
                    }
                }
            }
        )
    }
    if (isComposerExpanded) {
        ExpandedNoteComposer(
            inputTextFieldValue = inputTextFieldValue,
            onInputValueChange = { inputTextFieldValue = it },
            inputSpans = inputSpans,
            onSpansChange = { inputSpans = it },
            onClose = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                isComposerExpanded = false
            },
            onSendNote = {
                onSendNote()
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                isComposerExpanded = false
            }
        )
    }
    }
    }

    ThemeRevealLayers(
        currentDarkTheme = committedDarkTheme,
        targetDarkTheme = transitionTarget,
        revealProgress = revealProgress,
        toggleProgress = toggleProgress,
        canToggleTheme = canStartThemeReveal && !isThemeTransitioning,
        renderAppUi = renderAppUi
    )
}

private class CircularRevealClipShape(
    private val center: Offset,
    private val radius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            addOval(
                Rect(
                    left = center.x - radius,
                    top = center.y - radius,
                    right = center.x + radius,
                    bottom = center.y + radius
                )
            )
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun ThemeRevealLayers(
    currentDarkTheme: Boolean,
    targetDarkTheme: Boolean?,
    revealProgress: State<Float>,
    toggleProgress: State<Float>,
    canToggleTheme: Boolean,
    renderAppUi: @Composable (Boolean, Boolean, Boolean, (Offset) -> Unit) -> Unit
) {
    var viewportOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var toggleCenterInWindow by remember { mutableStateOf<Offset?>(null) }
    val isTransitioning = targetDarkTheme != null
    val revealCenter = toggleCenterInWindow?.minus(viewportOriginInWindow)
        ?: Offset(viewportSize.width / 2f, viewportSize.height / 2f)
    val revealRadius = calculateRevealRadius(revealCenter, viewportSize)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                val origin = coordinates.positionInWindow()
                if (viewportOriginInWindow != origin) viewportOriginInWindow = origin
                if (viewportSize != coordinates.size) viewportSize = coordinates.size
            }
    ) {
        val renderLayer: @Composable (Boolean, Boolean, Boolean) -> Unit = {
                layerDarkTheme,
                isRevealLayer,
                toggleEnabled ->
            OneLineNotesTheme(
                darkTheme = layerDarkTheme,
                animateThemeColors = false,
                toggleProgress = toggleProgress,
                manageSystemBars = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .background(HeaderBg)
                        )
                        val onTogglePositioned: (Offset) -> Unit = if (isRevealLayer) {
                            {}
                        } else {
                            { center -> toggleCenterInWindow = center }
                        }
                        renderAppUi(
                            layerDarkTheme,
                            isRevealLayer,
                            toggleEnabled,
                            onTogglePositioned
                        )
                    }
                }
            }
        }

        renderLayer(currentDarkTheme, false, canToggleTheme && !isTransitioning)

        targetDarkTheme?.let { newTheme ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = CircularRevealClipShape(
                            center = revealCenter,
                            radius = revealRadius * revealProgress.value
                        )
                    }
            ) {
                renderLayer(newTheme, true, false)
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clearAndSetSemantics { }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
        }
    }
}

private fun calculateRevealRadius(center: Offset, viewportSize: IntSize): Float {
    val width = viewportSize.width.toFloat()
    val height = viewportSize.height.toFloat()
    return maxOf(
        sqrt(center.x * center.x + center.y * center.y),
        sqrt((width - center.x) * (width - center.x) + center.y * center.y),
        sqrt(center.x * center.x + (height - center.y) * (height - center.y)),
        sqrt((width - center.x) * (width - center.x) + (height - center.y) * (height - center.y))
    ) + 2f
}
