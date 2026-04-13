package com.example.a212062_rimaniza_lab1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.a212062_rimaniza_lab1.R

// Local Font Family
val SpaceGrotesk = FontFamily(
    Font(R.font.spacegrotesk_regular, FontWeight.Normal),
    Font(R.font.spacegrotesk_bold, FontWeight.Bold)
)

// Default Material 3 typography values as baseline
val baseline = Typography()

val Typography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = SpaceGrotesk),
    displayMedium = baseline.displayMedium.copy(fontFamily = SpaceGrotesk),
    displaySmall = baseline.displaySmall.copy(fontFamily = SpaceGrotesk),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = SpaceGrotesk),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = SpaceGrotesk),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = SpaceGrotesk),
    titleLarge = baseline.titleLarge.copy(fontFamily = SpaceGrotesk),
    titleMedium = baseline.titleMedium.copy(fontFamily = SpaceGrotesk),
    titleSmall = baseline.titleSmall.copy(fontFamily = SpaceGrotesk),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = SpaceGrotesk),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = SpaceGrotesk),
    bodySmall = baseline.bodySmall.copy(fontFamily = SpaceGrotesk),
    labelLarge = baseline.labelLarge.copy(fontFamily = SpaceGrotesk),
    labelMedium = baseline.labelMedium.copy(fontFamily = SpaceGrotesk),
    labelSmall = baseline.labelSmall.copy(fontFamily = SpaceGrotesk),
)
