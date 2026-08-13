package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

private val DarkColorScheme = darkColorScheme(
    primary = MinimalLavenderPrimary,
    onPrimary = MinimalLavenderOnPrimary,
    primaryContainer = MinimalLavenderContainer,
    onPrimaryContainer = MinimalLavenderOnContainer,
    secondary = MinimalLavenderPrimary,
    onSecondary = MinimalLavenderOnPrimary,
    secondaryContainer = MinimalSurfaceElevated,
    onSecondaryContainer = MinimalTextPrimary,
    tertiary = MinimalEmerald,
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005234),
    onTertiaryContainer = Color(0xFF6CF8B8),
    background = MinimalDarkBackground,
    onBackground = MinimalTextPrimary,
    surface = MinimalCardBackground,
    onSurface = MinimalTextPrimary,
    surfaceVariant = MinimalSurfaceElevated,
    onSurfaceVariant = MinimalTextSecondary,
    outline = MinimalBorder,
    outlineVariant = MinimalBorderSubtle,
    surfaceContainer = MinimalSurfaceElevated,
    surfaceContainerHigh = Color(0xFF323038),
    error = MinimalRose,
    onError = MinimalRoseText
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalLightLavenderPrimary,
    onPrimary = MinimalLightLavenderOnPrimary,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = MinimalLightLavenderPrimary,
    onSecondary = Color.White,
    secondaryContainer = MinimalLightCardBackground,
    onSecondaryContainer = MinimalLightTextPrimary,
    tertiary = Color(0xFF10B981),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB9F6D6),
    onTertiaryContainer = Color(0xFF002113),
    background = MinimalLightBackground,
    onBackground = MinimalLightTextPrimary,
    surface = MinimalLightSurfaceElevated,
    onSurface = MinimalLightTextPrimary,
    surfaceVariant = MinimalLightCardBackground,
    onSurfaceVariant = MinimalLightTextSecondary,
    outline = MinimalLightBorder,
    outlineVariant = Color(0xFFE7E0EC),
    surfaceContainer = MinimalLightCardBackground,
    surfaceContainerHigh = Color(0xFFECE6F0),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun NotifVaultTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    NotifVaultTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        dynamicColor = dynamicColor,
        content = content
    )
}

