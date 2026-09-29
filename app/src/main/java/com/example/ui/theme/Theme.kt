package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeuroCyan,
    onPrimary = Color(0xFF00222B),
    primaryContainer = Color(0xFF004E5C),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = NeuroIndigo,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = NeuroAmber,
    onTertiary = Color(0xFF451A03),
    background = NeuroBackground,
    onBackground = TextPrimary,
    surface = NeuroSurface,
    onSurface = TextPrimary,
    surfaceVariant = NeuroSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderBright
)

@Composable
fun MiyaRejimiTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
