package com.cosytown.builder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SageDeep,
    onPrimary = Cream,
    secondary = Terracotta,
    onSecondary = Cream,
    tertiary = Amber,
    background = Cream,
    onBackground = Ink,
    surface = Fog,
    onSurface = Ink,
    surfaceVariant = Fog,
    onSurfaceVariant = SoftBrown,
)

private val DarkColors = darkColorScheme(
    primary = Sage,
    onPrimary = CreamDark,
    secondary = Terracotta,
    onSecondary = CreamDark,
    tertiary = Amber,
    background = CreamDark,
    onBackground = Cream,
    surface = Color(0xFF3A342B),
    onSurface = Cream,
    surfaceVariant = Color(0xFF3A342B),
    onSurfaceVariant = Fog,
)

@Composable
fun CosyTownBuilderTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = CosyTypography,
        content = content,
    )
}
