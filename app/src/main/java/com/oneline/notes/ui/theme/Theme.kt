package com.oneline.notes.ui.theme

import android.app.Activity
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkMessNoteColors.primaryBlue,
    secondary = DarkMessNoteColors.brightBlue,
    tertiary = DarkMessNoteColors.brightBlue,
    background = DarkMessNoteColors.bg,
    surface = DarkMessNoteColors.surfaceDark,
    surfaceVariant = DarkMessNoteColors.surfaceCard,
    onPrimary = DarkMessNoteColors.textPrimary,
    onBackground = DarkMessNoteColors.textPrimary,
    onSurface = DarkMessNoteColors.textPrimary,
    error = DarkMessNoteColors.destructiveAction,
    onError = DarkMessNoteColors.textPrimary,
    outline = DarkMessNoteColors.borderSubtle
)

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OneLineNotesTheme(
    content: @Composable () -> Unit
) {
    val appColors = DarkMessNoteColors
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = appColors.headerBg.toArgb()
            window.navigationBarColor = appColors.bg.toArgb()
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(LocalMessNoteColors provides appColors) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
