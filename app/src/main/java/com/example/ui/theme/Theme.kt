package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SleekPrimaryDark,
    onPrimary = SleekOnPrimaryDark,
    primaryContainer = SleekPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFF90CCF9),
    onSecondary = Color(0xFF003353),
    secondaryContainer = Color(0xFF004B76),
    onSecondaryContainer = Color(0xFFCCE5FF),
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color(0xFF3B2F00),
    tertiaryContainer = Color(0xFF554400),
    onTertiaryContainer = Color(0xFFFFE082),
    background = SleekBgDark,
    onBackground = Color(0xFFE2E2E6),
    surface = SleekSurfaceDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = SleekSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC4C7D0),
    outline = SleekBorderDark,
    outlineVariant = Color(0xFF3F5468),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = SleekNavyPrimary,
    onPrimary = Color.White,
    primaryContainer = SleekNavyContainer,
    onPrimaryContainer = SleekNavyOnContainer,
    secondary = SleekNavySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E2FF),
    onSecondaryContainer = Color(0xFF001945),
    tertiary = SleekTaskLabel,
    onTertiary = Color.White,
    tertiaryContainer = SleekTaskBg,
    onTertiaryContainer = SleekTaskText,
    background = SleekBgLight,
    onBackground = SleekTextPrimary,
    surface = SleekSurfaceLight,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurfaceVariantLight,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorderLight,
    outlineVariant = SleekBorderMuted,
    error = SleekUrgentLabel,
    onError = Color.White,
    errorContainer = SleekUrgentBg,
    onErrorContainer = SleekUrgentText
)

@Composable
fun QuickRemindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
