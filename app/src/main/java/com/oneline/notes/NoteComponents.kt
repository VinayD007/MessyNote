package com.oneline.notes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.oneline.notes.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// =============================================================================
// Multi-Selection Header
// =============================================================================
@Composable
fun MultiSelectHeader(
    selectedCount: Int,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onCopyPartially: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderRowHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Close button & selected count
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Exit selection mode",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "$selectedCount selected",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )
        }

        // TOP-RIGHT ACTIONS:
        // Exactly 1 selected: [Edit] [Copy] [Copy Partially] [Delete]
        // 2 or more selected: [Copy] [Delete]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedCount == 1) {
                // 1. Edit Action (Only for exactly 1 note)
                Surface(
                    shape = CircleShape,
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onEdit() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit),
                            contentDescription = "Edit message",
                            tint = BrightBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Copy Action
            Surface(
                shape = CircleShape,
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onCopy() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = "Copy selected",
                        tint = BrightBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (selectedCount == 1) {
                // 3. Copy Partially Action (Only for exactly 1 note)
                Surface(
                    shape = CircleShape,
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onCopyPartially() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_copy_partially),
                            contentDescription = "Copy partially",
                            tint = BrightBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 4. Delete Action
            Surface(
                shape = CircleShape,
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onDelete() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete selected",
                        tint = DestructiveAction,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// Reusable Star Icon
// =============================================================================
@Composable
fun StarIcon(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
    contentDescription: String? = null
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Canvas(
        modifier = modifier.then(semanticsModifier)
    ) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)
        if (minDim <= 0f) return@Canvas

        val strokeWidth = (minDim * 0.08f).coerceAtLeast(1.5f)
        val cx = w / 2f
        val cy = h / 2f

        val rOuter = (minDim / 2f) - (strokeWidth / 2f) - (minDim * 0.02f)
        val rInner = rOuter * 0.48f

        // 10 star vertices (0, 2, 4, 6, 8 are outer tips; 1, 3, 5, 7, 9 are inner valleys)
        val vertices = Array(10) { i ->
            val r = if (i % 2 == 0) rOuter else rInner
            val angle = -Math.PI / 2.0 + i * (Math.PI / 5.0)
            Offset(
                x = (cx + r * cos(angle)).toFloat(),
                y = (cy + r * sin(angle)).toFloat()
            )
        }

        val edgeLen = (vertices[1] - vertices[0]).getDistance()
        val rCornerOuter = edgeLen * 0.28f
        val rCornerInner = edgeLen * 0.20f

        val path = Path().apply {
            for (i in 0 until 10) {
                val prev = vertices[(i - 1 + 10) % 10]
                val curr = vertices[i]
                val next = vertices[(i + 1) % 10]

                val rCorner = if (i % 2 == 0) rCornerOuter else rCornerInner

                val vPrev = prev - curr
                val dPrev = vPrev.getDistance()
                val pIn = curr + vPrev * (rCorner / dPrev)

                val vNext = next - curr
                val dNext = vNext.getDistance()
                val pOut = curr + vNext * (rCorner / dNext)

                if (i == 0) {
                    moveTo(pIn.x, pIn.y)
                } else {
                    lineTo(pIn.x, pIn.y)
                }
                quadraticTo(curr.x, curr.y, pOut.x, pOut.y)
            }
            close()
        }

        if (isFavorite) {
            drawPath(
                path = path,
                color = tint,
                style = Fill
            )
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = strokeWidth,
                join = StrokeJoin.Round,
                cap = StrokeCap.Round
            )
        )
    }
}

// =============================================================================
// Normal Header
// =============================================================================
val HeaderRowHeight = 64.dp

@Composable
private fun ThemeModeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit
) {
    val progress = LocalThemeTransitionProgress.current
    val thumbOffset = lerp(28.dp, 4.dp, progress)

    Surface(
        shape = CircleShape,
        color = SurfaceCard,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .width(56.dp)
            .height(34.dp)
            .toggleable(
                value = isDarkTheme,
                role = Role.Switch,
                onValueChange = { onToggle() }
            )
            .semantics {
                contentDescription = if (isDarkTheme) "Dark theme enabled" else "Light theme enabled"
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                shape = CircleShape,
                color = PrimaryBlue,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "☀︎",
                        color = OnAccent,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer { alpha = progress }
                    )
                    Text(
                        text = "☾︎",
                        color = OnAccent,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer { alpha = 1f - progress }
                    )
                }
            }
        }
    }
}

@Composable
fun NormalHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSearchTrigger: () -> Unit,
    onFavoritesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderRowHeight)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messy Note",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(10.dp))
            ThemeModeToggle(
                isDarkTheme = isDarkTheme,
                onToggle = onThemeToggle
            )
        }

        // Top Right: Header Star & Search Trigger Button
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onFavoritesClick() },
                contentAlignment = Alignment.Center
            ) {
                StarIcon(
                    isFavorite = true,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Favorites"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onSearchTrigger() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Search notes",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// =============================================================================
// Favorites Header
// =============================================================================
@Composable
fun FavoritesHeader(
    onBack: () -> Unit,
    onSearchTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderRowHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Favorites",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                color = TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onSearchTrigger() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "Search favorites",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// =============================================================================
// Search Bar Header
// =============================================================================
@Composable
fun SearchBarHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchFocusRequester: FocusRequester,
    onExitSearch: () -> Unit,
    onSearchKeyboardDone: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search notes..."
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HeaderRowHeight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = SurfaceCard,
            border = BorderStroke(1.dp, BrightBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onExitSearch() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = "Exit search",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocusRequester),
                    textStyle = LocalTextStyle.current.copy(
                        color = TextPrimary,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(BrightBlue),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                placeholder,
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearchKeyboardDone()
                    })
                )

                // Single Clear X inside the search field
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Clear search",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Home Content Filter Control: [ All ] [ Notes ] [ Fields-Values ]
// =============================================================================
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeFilterControl(
    selectedFilter: NoteFilter,
    onFilterSelected: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val filters = NoteFilter.entries

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEachIndexed { index, filter ->
            val isSelected = selectedFilter == filter
            val bringIntoViewRequester = remember { BringIntoViewRequester() }

            LaunchedEffect(isSelected) {
                if (isSelected) {
                    bringIntoViewRequester.bringIntoView()
                }
            }

            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = { onFilterSelected(filter) },
                shapes = shapes,
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = SurfaceCard,
                    contentColor = TextSecondary,
                    checkedContainerColor = PrimaryBlue,
                    checkedContentColor = OnAccent
                ),
                border = BorderStroke(1.dp, if (isSelected) BrightBlue else BorderSubtle),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(44.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
            ) {
                Text(
                    text = filter.label,
                    fontFamily = GoogleSans,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}


// =============================================================================
// Interactive Swipeable Snackbar with Rounded Borders (Dismiss on swipe left or right)
// =============================================================================
@Composable
fun SwipeableSnackbar(
    snackbarData: SnackbarData,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val dismissThreshold = with(density) { 72.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.dp, BorderSubtle),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .graphicsLayer {
                this.alpha = alpha.value
            }
            .pointerInput(snackbarData) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (abs(offsetX.value) > dismissThreshold) {
                                val targetX = if (offsetX.value > 0) screenWidthPx else -screenWidthPx
                                launch {
                                    alpha.animateTo(0f, animationSpec = tween(140))
                                }
                                offsetX.animateTo(targetX, animationSpec = tween(160))
                                onDismiss()
                            } else {
                                launch {
                                    alpha.animateTo(1f, animationSpec = tween(150))
                                }
                                offsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            launch { alpha.animateTo(1f) }
                            offsetX.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                            val dragRatio = (abs(offsetX.value) / dismissThreshold).coerceIn(0f, 1f)
                            alpha.snapTo(1f - (dragRatio * 0.4f))
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = snackbarData.visuals.message,
                color = TextPrimary,
                fontFamily = GoogleSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            snackbarData.visuals.actionLabel?.let { actionLabel ->
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { snackbarData.performAction() }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionLabel,
                        fontFamily = GoogleSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrightBlue,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

val ActionButtonSize = 48.dp

// =============================================================================
// Notes Composer
// =============================================================================
@Composable
fun NotesComposer(
    inputTextFieldValue: TextFieldValue,
    onInputValueChange: (TextFieldValue) -> Unit,
    inputSpans: List<TextSpan>,
    onSpansChange: (List<TextSpan>) -> Unit,
    onSendNote: () -> Unit,
    onNewFieldClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Note Typing / Input Box
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val view = LocalView.current
                val currentInputValue = rememberUpdatedState(inputTextFieldValue)
                val currentSpansState = rememberUpdatedState(inputSpans)

                val composerTextToolbar = remember(view) {
                    MonoTextToolbar(
                        view = view,
                        onMonoRequested = {
                            val valNow = currentInputValue.value
                            val sel = valNow.selection
                            if (!sel.collapsed && sel.length > 0) {
                                onSpansChange(
                                    toggleMonospaceSpan(
                                        valNow.text.length,
                                        currentSpansState.value,
                                        sel.min,
                                        sel.max
                                    )
                                )
                                onInputValueChange(valNow.copy(selection = sel))
                            }
                        },
                        canApplyMono = {
                            val sel = currentInputValue.value.selection
                            !sel.collapsed && sel.length > 0
                        }
                    )
                }

                val currentMonoText = MonospaceText
                val currentMonoBg = MonospaceBg

                CompositionLocalProvider(LocalTextToolbar provides composerTextToolbar) {
                    OutlinedTextField(
                        value = inputTextFieldValue,
                        onValueChange = { newVal ->
                            val oldText = inputTextFieldValue.text
                            val newText = newVal.text
                            if (oldText != newText) {
                                onSpansChange(adjustSpansForTextChange(oldText, newText, inputSpans))
                            }
                            onInputValueChange(newVal)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        placeholder = {
                            Text(
                                "Type a note...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        visualTransformation = remember(inputSpans, currentMonoText, currentMonoBg) {
                            VisualTransformation { text ->
                                TransformedText(
                                    buildMonospaceAnnotatedString(text.text, inputSpans, monoColor = currentMonoText, monoBackground = currentMonoBg),
                                    OffsetMapping.Identity
                                )
                            }
                        },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = BrightBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = BrightBlue
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onSendNote() })
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Send Button
                val sendButtonShape = CircleShape
                Surface(
                    shape = sendButtonShape,
                    color = PrimaryBlue,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(ActionButtonSize)
                        .clip(sendButtonShape)
                        .clickable { onSendNote() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = "Send note",
                            tint = OnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

// =============================================================================
// Notes Empty State
// =============================================================================
@Composable
fun NotesEmptyState(
    isSearchActive: Boolean,
    searchQuery: String,
    selectedFilter: NoteFilter,
    modifier: Modifier = Modifier,
    isFavorites: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (isSearchActive) Modifier.imePadding() else Modifier)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(if (isSearchActive && searchQuery.isNotBlank()) R.drawable.ic_search else R.drawable.ic_chat),
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFavorites) {
                if (isSearchActive && searchQuery.isNotBlank()) "No matching favorites" else "No favorites yet"
            } else if (isSearchActive && searchQuery.isNotBlank()) {
                "No notes matching \"$searchQuery\""
            } else {
                when (selectedFilter) {
                    NoteFilter.ALL, NoteFilter.NOTES -> "No notes yet"
                    NoteFilter.FIELDS_VALUES -> "No Fields-Values yet"
                    NoteFilter.LISTS -> "No Lists yet"
                }
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// Reusable Notes List Viewport
// =============================================================================
@Composable
fun NotesList(
    displayedNotes: List<NoteItem>,
    listState: LazyListState,
    selectedNoteIds: SnapshotStateList<String>,
    isMultiSelectMode: Boolean,
    isSearchActive: Boolean,
    hasBottomFab: Boolean,
    onNoteClick: (NoteItem) -> Unit,
    onNoteLongClick: (NoteItem) -> Unit,
    onMonoTap: (String) -> Unit,
    emptyState: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    fabOverlay: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(modifier = modifier) {
        val fabButtonHeight = ActionButtonSize
        val fabButtonSpacing = 8.dp
        val listBottomPadding = if (hasBottomFab) {
            fabButtonHeight + fabButtonSpacing
        } else {
            0.dp
        }

        if (displayedNotes.isEmpty()) {
            emptyState()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isSearchActive) Modifier.imePadding() else Modifier)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = listBottomPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedNotes, key = { it.id }) { note ->
                    val isSelected = selectedNoteIds.contains(note.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isMultiSelectMode) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BrightBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = "Selected",
                                        tint = OnAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            } else {
                                Spacer(modifier = Modifier.width(34.dp))
                            }
                        }

                        SentNoteBubble(
                            note = note,
                            isSelected = isSelected,
                            isSelectionMode = isMultiSelectMode,
                            modifier = if (note.type == NoteType.FIELD_VALUE) {
                                Modifier
                                    .weight(1f)
                                    .padding(end = 16.dp)
                            } else {
                                Modifier.weight(1f)
                            },
                            onClick = { onNoteClick(note) },
                            onLongClick = { onNoteLongClick(note) },
                            onMonoTap = onMonoTap
                        )
                    }
                }
            }
        }

        if (fabOverlay != null) {
            fabOverlay()
        }
    }
}
