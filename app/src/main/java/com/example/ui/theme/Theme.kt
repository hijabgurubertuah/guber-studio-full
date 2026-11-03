package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkColorSchemePrimary,
        secondary = DarkColorSchemeSecondary,
        tertiary = ToskaBright,
        background = DarkColorSchemeBackground,
        surface = DarkColorSchemeSurface,
        onPrimary = Color(0xFF041E1E),
        onSecondary = Color(0xFF041E1E),
        onBackground = Color(0xFFE2E8F0),
        onSurface = Color(0xFFE2E8F0)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = ToskaBright,
        secondary = MalayGold,
        tertiary = ToskaTeal,
        background = ToskaDark,
        surface = ToskaDarkMedium,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color(0xFFE2E8F0),
        onSurface = Color(0xFFE2E8F0)
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

