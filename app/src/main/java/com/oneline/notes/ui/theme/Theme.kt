package com.oneline.notes.ui.theme

import android.app.Activity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.first

private const val ThemeTransitionMillis = 560

/** Progress shared with the header toggle without making the whole UI recompose each frame. */
val LocalThemeTransitionProgress = compositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

private val DarkColorScheme = darkColorScheme(
    primary = DarkMessNoteColors.primaryBlue,
    onPrimary = DarkMessNoteColors.onAccent,
    secondary = DarkMessNoteColors.brightBlue,
    onSecondary = DarkMessNoteColors.onAccent,
    tertiary = DarkMessNoteColors.brightBlue,
    background = DarkMessNoteColors.bg,
    onBackground = DarkMessNoteColors.textPrimary,
    surface = DarkMessNoteColors.surfaceDark,
    onSurface = DarkMessNoteColors.textPrimary,
    surfaceVariant = DarkMessNoteColors.surfaceCard,
    onSurfaceVariant = DarkMessNoteColors.textSecondary,
    error = DarkMessNoteColors.destructiveAction,
    onError = DarkMessNoteColors.onAccent,
    outline = DarkMessNoteColors.borderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = LightMessNoteColors.primaryBlue,
    onPrimary = LightMessNoteColors.onAccent,
    secondary = LightMessNoteColors.brightBlue,
    onSecondary = LightMessNoteColors.onAccent,
    tertiary = LightMessNoteColors.brightBlue,
    background = LightMessNoteColors.bg,
    onBackground = LightMessNoteColors.textPrimary,
    surface = LightMessNoteColors.surfaceDark,
    onSurface = LightMessNoteColors.textPrimary,
    surfaceVariant = LightMessNoteColors.surfaceCard,
    onSurfaceVariant = LightMessNoteColors.textSecondary,
    error = LightMessNoteColors.destructiveAction,
    onError = LightMessNoteColors.onAccent,
    outline = LightMessNoteColors.borderSubtle
)

private fun Color.blendTo(other: Color, fraction: Float): Color =
    lerp(this, other, fraction)

private fun interpolateColors(
    dark: MessNoteColors,
    light: MessNoteColors,
    progress: Float
) = MessNoteColors(
    bg = dark.bg.blendTo(light.bg, progress),
    headerBg = dark.headerBg.blendTo(light.headerBg, progress),
    surfaceCard = dark.surfaceCard.blendTo(light.surfaceCard, progress),
    noteBubbleBackground = dark.noteBubbleBackground.blendTo(light.noteBubbleBackground, progress),
    noteBubbleAccent = dark.noteBubbleAccent.blendTo(light.noteBubbleAccent, progress),
    surfaceDark = dark.surfaceDark.blendTo(light.surfaceDark, progress),
    surfaceElevated = dark.surfaceElevated.blendTo(light.surfaceElevated, progress),
    primaryBlue = dark.primaryBlue.blendTo(light.primaryBlue, progress),
    brightBlue = dark.brightBlue.blendTo(light.brightBlue, progress),
    onAccent = dark.onAccent.blendTo(light.onAccent, progress),
    selectedItemBackground = dark.selectedItemBackground.blendTo(light.selectedItemBackground, progress),
    destructiveAction = dark.destructiveAction.blendTo(light.destructiveAction, progress),
    monospaceBg = dark.monospaceBg.blendTo(light.monospaceBg, progress),
    monospaceText = dark.monospaceText.blendTo(light.monospaceText, progress),
    linkAccent = dark.linkAccent.blendTo(light.linkAccent, progress),
    textPrimary = dark.textPrimary.blendTo(light.textPrimary, progress),
    textSecondary = dark.textSecondary.blendTo(light.textSecondary, progress),
    textMuted = dark.textMuted.blendTo(light.textMuted, progress),
    borderSubtle = dark.borderSubtle.blendTo(light.borderSubtle, progress)
)

private fun ColorScheme.blendTo(other: ColorScheme, progress: Float): ColorScheme = copy(
    primary = primary.blendTo(other.primary, progress),
    onPrimary = onPrimary.blendTo(other.onPrimary, progress),
    primaryContainer = primaryContainer.blendTo(other.primaryContainer, progress),
    onPrimaryContainer = onPrimaryContainer.blendTo(other.onPrimaryContainer, progress),
    inversePrimary = inversePrimary.blendTo(other.inversePrimary, progress),
    secondary = secondary.blendTo(other.secondary, progress),
    onSecondary = onSecondary.blendTo(other.onSecondary, progress),
    secondaryContainer = secondaryContainer.blendTo(other.secondaryContainer, progress),
    onSecondaryContainer = onSecondaryContainer.blendTo(other.onSecondaryContainer, progress),
    tertiary = tertiary.blendTo(other.tertiary, progress),
    onTertiary = onTertiary.blendTo(other.onTertiary, progress),
    tertiaryContainer = tertiaryContainer.blendTo(other.tertiaryContainer, progress),
    onTertiaryContainer = onTertiaryContainer.blendTo(other.onTertiaryContainer, progress),
    background = background.blendTo(other.background, progress),
    onBackground = onBackground.blendTo(other.onBackground, progress),
    surface = surface.blendTo(other.surface, progress),
    onSurface = onSurface.blendTo(other.onSurface, progress),
    surfaceVariant = surfaceVariant.blendTo(other.surfaceVariant, progress),
    onSurfaceVariant = onSurfaceVariant.blendTo(other.onSurfaceVariant, progress),
    surfaceTint = surfaceTint.blendTo(other.surfaceTint, progress),
    inverseSurface = inverseSurface.blendTo(other.inverseSurface, progress),
    inverseOnSurface = inverseOnSurface.blendTo(other.inverseOnSurface, progress),
    error = error.blendTo(other.error, progress),
    onError = onError.blendTo(other.onError, progress),
    errorContainer = errorContainer.blendTo(other.errorContainer, progress),
    onErrorContainer = onErrorContainer.blendTo(other.onErrorContainer, progress),
    outline = outline.blendTo(other.outline, progress),
    outlineVariant = outlineVariant.blendTo(other.outlineVariant, progress),
    scrim = scrim.blendTo(other.scrim, progress),
    surfaceBright = surfaceBright.blendTo(other.surfaceBright, progress),
    surfaceDim = surfaceDim.blendTo(other.surfaceDim, progress),
    surfaceContainer = surfaceContainer.blendTo(other.surfaceContainer, progress),
    surfaceContainerHigh = surfaceContainerHigh.blendTo(other.surfaceContainerHigh, progress),
    surfaceContainerHighest = surfaceContainerHighest.blendTo(other.surfaceContainerHighest, progress),
    surfaceContainerLow = surfaceContainerLow.blendTo(other.surfaceContainerLow, progress),
    surfaceContainerLowest = surfaceContainerLowest.blendTo(other.surfaceContainerLowest, progress),
    primaryFixed = primaryFixed.blendTo(other.primaryFixed, progress),
    primaryFixedDim = primaryFixedDim.blendTo(other.primaryFixedDim, progress),
    onPrimaryFixed = onPrimaryFixed.blendTo(other.onPrimaryFixed, progress),
    onPrimaryFixedVariant = onPrimaryFixedVariant.blendTo(other.onPrimaryFixedVariant, progress),
    secondaryFixed = secondaryFixed.blendTo(other.secondaryFixed, progress),
    secondaryFixedDim = secondaryFixedDim.blendTo(other.secondaryFixedDim, progress),
    onSecondaryFixed = onSecondaryFixed.blendTo(other.onSecondaryFixed, progress),
    onSecondaryFixedVariant = onSecondaryFixedVariant.blendTo(other.onSecondaryFixedVariant, progress),
    tertiaryFixed = tertiaryFixed.blendTo(other.tertiaryFixed, progress),
    tertiaryFixedDim = tertiaryFixedDim.blendTo(other.tertiaryFixedDim, progress),
    onTertiaryFixed = onTertiaryFixed.blendTo(other.onTertiaryFixed, progress),
    onTertiaryFixedVariant = onTertiaryFixedVariant.blendTo(other.onTertiaryFixedVariant, progress)
)

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OneLineNotesTheme(
    darkTheme: Boolean,
    animateThemeColors: Boolean = true,
    toggleProgress: State<Float>? = null,
    manageSystemBars: Boolean = true,
    content: @Composable () -> Unit
) {
    val animatedProgressState = if (animateThemeColors) {
        val transition = updateTransition(targetState = darkTheme, label = "appTheme")
        transition.animateFloat(
            transitionSpec = {
                tween(
                    durationMillis = ThemeTransitionMillis,
                    easing = FastOutSlowInEasing
                )
            },
            label = "themeProgress"
        ) { isDark -> if (isDark) 0f else 1f }
    } else {
        null
    }
    val endpointProgress = if (darkTheme) 0f else 1f
    val paletteProgress = animatedProgressState?.value ?: endpointProgress
    val appColors = if (animateThemeColors) {
        interpolateColors(DarkMessNoteColors, LightMessNoteColors, paletteProgress)
    } else if (darkTheme) {
        DarkMessNoteColors
    } else {
        LightMessNoteColors
    }
    val colorScheme = if (animateThemeColors) {
        DarkColorScheme.blendTo(LightColorScheme, paletteProgress)
    } else if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    val localToggleProgress = toggleProgress ?: remember(darkTheme, animatedProgressState) {
        derivedStateOf { animatedProgressState?.value ?: endpointProgress }
    }

    val view = LocalView.current
    if (manageSystemBars) {
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = appColors.headerBg.toArgb()
                window.navigationBarColor = appColors.bg.toArgb()
            }
        }

        LaunchedEffect(darkTheme) {
            withFrameNanos { }
            animatedProgressState?.let { progressState ->
                snapshotFlow { progressState.value }.first { value ->
                    if (darkTheme) value <= 0.5f else value >= 0.5f
                }
            }
            if (!view.isInEditMode) {
                val window = (view.context as Activity).window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalMessNoteColors provides appColors,
        LocalThemeTransitionProgress provides localToggleProgress
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
