package com.realitycheck.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.realitycheck.app.data.ThemePreferences

/**
 * Modern Dark Color Scheme - 2025 Design Standards
 * Improved contrast ratios and semantic color usage
 */
private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = BrandPrimaryLight,
    
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrandSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = BrandSecondary,
    
    tertiary = BrandAccent,
    onTertiary = Color.White,
    tertiaryContainer = BrandAccent.copy(alpha = 0.2f),
    onTertiaryContainer = BrandAccent,
    
    background = BackgroundDark,
    onBackground = Color(0xFFE5E5E5),
    
    surface = SurfaceDark,
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB0B0B0),
    
    error = BrandError,
    onError = Color.White,
    errorContainer = BrandError.copy(alpha = 0.2f),
    onErrorContainer = BrandErrorLight,
    
    outline = Color(0xFF3A3A3C),
    outlineVariant = Color(0xFF2C2C2E),
    
    inverseSurface = Color(0xFFE5E5E5),
    inverseOnSurface = Color(0xFF1E1E1E),
    inversePrimary = BrandPrimaryDark,
    
    scrim = Color.Black.copy(alpha = 0.5f),
    surfaceBright = SurfaceElevatedDark,
    surfaceDim = SurfaceDark,
)

/**
 * Modern Light Color Scheme - 2025 Design Standards
 * Improved contrast ratios and semantic color usage
 */
private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = BrandPrimaryDark,
    
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrandSecondary.copy(alpha = 0.12f),
    onSecondaryContainer = BrandPrimaryDark,
    
    tertiary = BrandAccent,
    onTertiary = Color.White,
    tertiaryContainer = BrandAccent.copy(alpha = 0.12f),
    onTertiaryContainer = BrandPrimaryDark,
    
    background = BackgroundLight,
    onBackground = Color(0xFF1A1A1A),
    
    surface = SurfaceElevated,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF4A4A4A),
    
    error = BrandError,
    onError = Color.White,
    errorContainer = BrandError.copy(alpha = 0.12f),
    onErrorContainer = BrandErrorDark,
    
    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFE5E5E5),
    
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = BrandPrimaryLight,
    
    scrim = Color.Black.copy(alpha = 0.3f),
    surfaceBright = SurfaceElevated,
    surfaceDim = SurfaceVariantLight,
)

/**
 * RealityCheck Theme - Modern 2025 Design System
 * 
 * Features:
 * - Material Design 3 color scheme
 * - Dynamic color support (Android 12+)
 * - Improved contrast ratios for accessibility
 * - Smooth dark/light mode transitions
 */
@Composable
fun RealityCheckTheme(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = true,
    themePreferences: ThemePreferences? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    
    // Determine dark theme based on preference or system
    val isDarkTheme = when {
        darkTheme != null -> darkTheme
        themePreferences != null -> {
            when (themePreferences.getThemeMode()) {
                ThemePreferences.ThemeMode.DARK -> true
                ThemePreferences.ThemeMode.LIGHT -> false
                ThemePreferences.ThemeMode.SYSTEM -> systemDarkTheme
            }
        }
        else -> systemDarkTheme
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) {
                dynamicDarkColorScheme(context).copy(
                    // Override with brand colors while maintaining dynamic theming
                    primary = BrandPrimary,
                    secondary = BrandSecondary,
                    tertiary = BrandAccent
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    primary = BrandPrimary,
                    secondary = BrandSecondary,
                    tertiary = BrandAccent
                )
            }
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use surface color for status bar for better visual integration
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Extension for easy access to brand colors from theme
val MaterialTheme.brandPrimary: Color
    @Composable get() = BrandPrimary

val MaterialTheme.brandSuccess: Color
    @Composable get() = BrandSuccess

val MaterialTheme.brandError: Color
    @Composable get() = BrandError

