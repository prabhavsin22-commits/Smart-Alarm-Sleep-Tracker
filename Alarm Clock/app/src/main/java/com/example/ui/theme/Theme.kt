package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    DEEP_INDIGO,
    AMOLED_BLACK,
    BEDSIDE_REDSHIFT
}

private val IndigoColorScheme = darkColorScheme(
    primary = SunriseGold,
    onPrimary = Color(0xFF261900),
    primaryContainer = SunriseDeepAmber,
    onPrimaryContainer = SunriseBrightSun,
    secondary = SleepRemLavender,
    onSecondary = Color(0xFF1E0F38),
    secondaryContainer = Color(0xFF332052),
    onSecondaryContainer = SleepRemLavender,
    tertiary = AlertCyan,
    background = AuraBackgroundIndigo,
    onBackground = AuraTextPrimary,
    surface = AuraSurfaceDark,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraSurfaceCard,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraDivider
)

private val AmoledColorScheme = darkColorScheme(
    primary = SunriseGold,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF381500),
    onPrimaryContainer = SunriseGold,
    secondary = SleepRemLavender,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF23183B),
    onSecondaryContainer = SleepRemLavender,
    tertiary = AlertCyan,
    background = Color.Black,
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0C0C0E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF151518),
    onSurfaceVariant = Color(0xFFB0B0B8),
    outline = Color(0xFF24242A)
)

private val RedshiftColorScheme = darkColorScheme(
    primary = Color(0xFFFF5252),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A0000),
    onPrimaryContainer = Color(0xFFFF8A80),
    secondary = Color(0xFFFF8A80),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3B0B0B),
    onSecondaryContainer = Color(0xFFFFCDD2),
    tertiary = Color(0xFFFFAB91),
    background = Color(0xFF080202),
    onBackground = Color(0xFFFFCDD2),
    surface = Color(0xFF140505),
    onSurface = Color(0xFFFFCDD2),
    surfaceVariant = Color(0xFF240A0A),
    onSurfaceVariant = Color(0xFFE57373),
    outline = Color(0xFF3E1212)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DEEP_INDIGO,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.DEEP_INDIGO -> IndigoColorScheme
        AppThemeMode.AMOLED_BLACK -> AmoledColorScheme
        AppThemeMode.BEDSIDE_REDSHIFT -> RedshiftColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
