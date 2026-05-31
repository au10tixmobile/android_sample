package com.au10tix.integration.sample.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Au10tixBlue,
    onPrimary = TextPrimary,
    secondary = Au10tixBlue,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = RedError,
    onError = TextPrimary
)

@Composable
fun Au10tixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
