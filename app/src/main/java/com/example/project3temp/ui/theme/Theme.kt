package com.example.project3temp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    secondary = BrandOrangeSoft,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    secondary = BrandOrangeSoft,
    tertiary = Pink40,
    background = BrandBackground,
    surface = BrandBackground,
)

@Composable
fun Project3tempTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
