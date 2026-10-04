package com.oneline.notes.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class MessNoteColors(
    val bg: Color,
    val headerBg: Color,
    val surfaceCard: Color,
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
    bg = Color(0xFF14151B),
    headerBg = Color(0xFF181A21),
    surfaceCard = Color(0xFF1E222B),
    surfaceDark = Color(0xFF252A35),
    surfaceElevated = Color(0xFF252A35),
    primaryBlue = Color(0xFF1D9BF0),
    brightBlue = Color(0xFF1D9BF0),
    onAccent = Color(0xFFFFFFFF),
    selectedItemBackground = Color(0xFF1E3250),
    destructiveAction = Color(0xFFEF5350),
    monospaceBg = Color(0xFF121620),
    monospaceText = Color(0xFF648EEA),
    linkAccent = Color(0xFF64B5F6),
    textPrimary = Color(0xFFEDEDEF),
    textSecondary = Color(0xFFA0A2A9),
    textMuted = Color(0xFFA0A2A9),
    borderSubtle = Color(0xFF2E3440)
)

val LightMessNoteColors = MessNoteColors(
    bg = Color(0xFFF5F6F8),
    headerBg = Color(0xFFFFFFFF),
    surfaceCard = Color(0xFFFFFFFF),
    surfaceDark = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF0F2F5),
    primaryBlue = Color(0xFF1D9BF0),
    brightBlue = Color(0xFF0878C9),
    onAccent = Color(0xFFFFFFFF),
    selectedItemBackground = Color(0xFFE5F2FC),
    destructiveAction = Color(0xFFC62828),
    monospaceBg = Color(0xFFEAF1FA),
    monospaceText = Color(0xFF315EA8),
    linkAccent = Color(0xFF004296),
    textPrimary = Color(0xFF202124),
    textSecondary = Color(0xFF5F6368),
    textMuted = Color(0xFF72767D),
    borderSubtle = Color(0xFFD7DBE0)
)

val LocalMessNoteColors = compositionLocalOf { DarkMessNoteColors }

val BgDark: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.bg
val HeaderBg: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.headerBg

val SurfaceCard: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceCard
val SentBubbleColor: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceCard

val SurfaceDark: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceDark
val SurfaceElevated: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.surfaceElevated

val PrimaryBlue: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.primaryBlue
val PrimaryIndigo: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.primaryBlue

val BrightBlue: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.brightBlue
val ActiveAccent: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.brightBlue
val AccentCyan: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.brightBlue
val SecondaryViolet: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.brightBlue

val SelectedItemBackground: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.selectedItemBackground
val DestructiveAction: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.destructiveAction
val AccentDanger: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.destructiveAction

val MonospaceBg: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.monospaceBg
val MonospaceText: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.monospaceText

val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.textPrimary
val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.textSecondary
val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.textMuted
val BorderSubtle: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.borderSubtle
val OnAccent: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.onAccent
val LinkAccent: Color @Composable @ReadOnlyComposable get() = LocalMessNoteColors.current.linkAccent
