package com.oneline.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.oneline.notes.R

val GoogleSans = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.W400),
    Font(R.font.google_sans_medium, FontWeight.W500),
    Font(R.font.google_sans_bold, FontWeight.W700)
)

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = GoogleSans),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = GoogleSans),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = GoogleSans),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = GoogleSans),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = GoogleSans),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = GoogleSans),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = GoogleSans),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = GoogleSans),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = GoogleSans),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = GoogleSans),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = GoogleSans),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = GoogleSans),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = GoogleSans),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = GoogleSans),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = GoogleSans)
)
