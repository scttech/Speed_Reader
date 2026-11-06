package com.scttech.android.kotlin.speedreader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette taken from the "Speed Reader 0 to 100" logo.
val RaceBackground = Color(0xFF0B0E14)
val RaceSurface = Color(0xFF151A24)
val RaceBlue = Color(0xFF2D9CFF)
val RaceYellow = Color(0xFFFFD60A)
val RaceOrange = Color(0xFFFF9F1C)
val RaceRed = Color(0xFFFF3B1F)
val RaceWhite = Color(0xFFF4F6FA)

private val SpeedReaderColors = darkColorScheme(
    primary = RaceOrange,
    onPrimary = Color.Black,
    secondary = RaceBlue,
    onSecondary = Color.Black,
    tertiary = RaceYellow,
    background = RaceBackground,
    onBackground = RaceWhite,
    surface = RaceSurface,
    onSurface = RaceWhite,
    surfaceVariant = Color(0xFF1E2533),
    onSurfaceVariant = Color(0xFFB8C0D0),
    error = RaceRed,
)

/** App is intentionally always dark to match the racing-gauge branding. */
@Composable
fun SpeedReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SpeedReaderColors, content = content)
}
