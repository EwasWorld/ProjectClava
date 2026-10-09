package com.eywa.projectclava.main.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme as M3MaterialTheme

private val DarkColorPalette = darkColors(
        primary = Purple200,
        primaryVariant = Purple700,
        secondary = Teal200
)

private val LightColorPalette = lightColors(
        primary = Purple500,
        primaryVariant = Purple700,
        secondary = Teal200

        /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun ProjectClavaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    }
    else {
        LightColorPalette
    }

    MaterialTheme(
            colors = LightColorPalette, // TODO Dark theme
            typography = Typography,
            shapes = Shapes,
            content = content
    )
}

/**
 * Material3 theme matching [ProjectClavaTheme]
 */
@Composable
fun ClavaTheme(content: @Composable () -> Unit) {
    M3MaterialTheme(
        colorScheme = lightColorScheme(primary = Purple500),
        content = content,
    )
}
