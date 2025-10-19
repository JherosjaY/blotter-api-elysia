package com.example.blottermanagementsystem.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = ElectricBlueDark,
    onPrimaryContainer = TextPrimary,
    
    secondary = InfoBlue,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceLight,
    onSecondaryContainer = TextSecondary,
    
    tertiary = ElectricBlueLight,
    onTertiary = TextPrimary,
    
    background = DarkNavy,
    onBackground = TextPrimary,
    
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    
    error = ErrorRed,
    onError = TextPrimary,
    
    outline = DividerColor,
    outlineVariant = ShimmerColor
)

// Light Theme Color Scheme - Soft & Professional
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryLight,
    onPrimaryContainer = LightTextPrimary,
    
    secondary = LightInfoBlue,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextSecondary,
    
    tertiary = LightPrimaryLight,
    onTertiary = Color.White,
    
    background = LightBackground,
    onBackground = LightTextPrimary,
    
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    
    error = LightErrorRed,
    onError = Color.White,
    
    outline = LightDivider,
    outlineVariant = LightBorder
)

@Composable
fun BlotterManagementSystemTheme(
    darkTheme: Boolean = true, // Always use dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Always dark
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavy.toArgb()
            window.navigationBarColor = DarkNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}