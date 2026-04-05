package com.yeudaby.sixheaven.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary              = Burgundy,
    onPrimary            = Color.White,
    primaryContainer     = BurgundyLight,
    onPrimaryContainer   = Color(0xFF3F0018),
    secondary            = SkyBlue,
    onSecondary          = Color.White,
    secondaryContainer   = SkyBlueLight,
    onSecondaryContainer = Color(0xFF001F29),
    tertiary             = SageGreen,
    onTertiary           = Color.White,
    tertiaryContainer    = SageGreenLight,
    background           = OffWhite,
    surface              = OffWhite,
    surfaceVariant       = Color(0xFFEFF1F5),
    onBackground         = TextPrimary,
    onSurface            = TextPrimary,
    onSurfaceVariant     = TextSecondary,
    outline              = Color(0xFFE2E8F0),
    error                = Color(0xFFDC2626),
    onError              = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary    = Color(0xFFFFB3C6),
    secondary  = SkyBlue,
    background = Color(0xFF0F172A),
    surface    = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface    = Color(0xFFF1F5F9)
)

@Composable
fun SixHeavenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Pin to brand colors — dynamic color would dilute the SixHeaven identity
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
