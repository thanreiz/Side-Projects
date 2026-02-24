package com.floapp.agriflo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FloLightColorScheme = lightColorScheme(
    primary = FloGreen700,
    onPrimary = FloWhite,
    primaryContainer = FloGreen200,
    onPrimaryContainer = FloGreen700,
    secondary = FloEarth700,
    onSecondary = FloWhite,
    secondaryContainer = FloEarth200,
    onSecondaryContainer = FloEarth700,
    tertiary = FloGold500,
    onTertiary = FloGray900,
    error = FloRed500,
    onError = FloWhite,
    errorContainer = FloRed100,
    onErrorContainer = FloRed500,
    background = FloBackground,
    onBackground = FloOnBackground,
    surface = FloSurface,
    onSurface = FloOnSurface,
    surfaceVariant = FloGreen50,
    outline = FloGreen200
)

private val FloDarkColorScheme = darkColorScheme(
    primary = FloGreen500,
    onPrimary = Color(0xFF003300),
    primaryContainer = FloGreen700,
    onPrimaryContainer = FloGreen200,
    secondary = FloEarth500,
    onSecondary = Color(0xFF2C1500),
    background = Color(0xFF101810),
    onBackground = Color(0xFFE0F2E0),
    surface = Color(0xFF1A251A),
    onSurface = Color(0xFFE0F2E0),
    tertiary = FloGold500,
    onTertiary = Color(0xFF1A0000)
)

@Composable
fun FloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FloDarkColorScheme else FloLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FloTypography,
        content = content
    )
}
