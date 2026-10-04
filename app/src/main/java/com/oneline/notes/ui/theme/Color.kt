package com.oneline.notes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class MessNoteColors(
    val bg: Color,
    val headerBg: Color,
    val surfaceCard: Color,
    val noteBubbleBackground: Color,
    val noteBubbleAccent: Color,
    val surfaceDark: Color,
    val surfaceElevated: Color,
    val primaryBlue: Color,
    val brightBlue: Color,
    val onAccent: Color,
    val selectedItemBackground: Color,
    val destructiveAction: Color,
    val monospaceBg: Color,
    val monospaceText: Color,
    val linkAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val borderSubtle: Color
)

val DarkMessNoteColors = MessNoteColors(
    bg = Color(0xFF000000),
    headerBg = Color(0xFF1C1C1E),
    surfaceCard = Color(0xFF1C1C1E),
    noteBubbleBackground = Color(0xFF000000),
    noteBubbleAccent = Color(0xFFFFD60A),
    surfaceDark = Color(0xFF1C1C1E),
    surfaceElevated = Color(0xFF1C1C1E),
    primaryBlue = Color(0xFFFFD60A),
    brightBlue = Color(0xFFFFD60A),
    onAccent = Color(0xFF000000),
    selectedItemBackground = Color(0xFF3A3310),
    destructiveAction = Color(0xFFEF5350),
    monospaceBg = Color(0xFF1C1C1E),
    monospaceText = Color(0xFFFFD60A),
    linkAccent = Color(0xFFFFD60A),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0B0B5),
    textMuted = Color(0xFF8E8E93),
    borderSubtle = Color(0xFF38383A)
)

val LightMessNoteColors = MessNoteColors(
    bg = Color(0xFFFFFFFF),
    headerBg = Color(0xFFF2F2F7),
    surfaceCard = Color(0xFFF2F2F7),
    noteBubbleBackground = Color(0xFFFFFFFF),
    noteBubbleAccent = Color(0xFFFFCC00),
    surfaceDark = Color(0xFFF2F2F7),
    surfaceElevated = Color(0xFFF2F2F7),
    primaryBlue = Color(0xFFFFCC00),
    brightBlue = Color(0xFFFFCC00),
    onAccent = Color(0xFF000000),
    selectedItemBackground = Color(0xFFFFF2B2),
    destructiveAction = Color(0xFFC62828),
    monospaceBg = Color(0xFFF2F2F7),
    monospaceText = Color(0xFF6B5200),
    linkAccent = Color(0xFF6B5200),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF5C5C60),
    textMuted = Color(0xFF76767A),
    borderSubtle = Color(0xFFD1D1D6)
)

val LocalMessNoteColors = compositionLocalOf { DarkMessNoteColors }

val BgDark: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.bg
val HeaderBg: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.headerBg

val SurfaceCard: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceCard
val NoteBubbleAccent: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.noteBubbleAccent

val SurfaceDark: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceDark
val SurfaceElevated: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceElevated

val PrimaryBlue: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.primaryBlue
val BrightBlue: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.brightBlue

val DestructiveAction: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.destructiveAction

val MonospaceBg: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.monospaceBg
val MonospaceText: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.monospaceText

val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.textSecondary
val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.textMuted
val BorderSubtle: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.borderSubtle
val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.onAccent
val LinkAccent: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.linkAccent
