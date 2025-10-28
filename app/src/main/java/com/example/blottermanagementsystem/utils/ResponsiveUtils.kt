package com.example.blottermanagementsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ResponsiveUtils - Automatic screen size adaptation
 * Makes UI perfect on ALL Android devices (phones & tablets, old & new)
 */

// Screen size categories
enum class ScreenSize {
    SMALL,      // Small phones (< 360dp width)
    COMPACT,    // Normal phones (360-600dp width)
    MEDIUM,     // Large phones / Small tablets (600-840dp width)
    EXPANDED    // Tablets (> 840dp width)
}

// Get current screen size category
@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    return when {
        screenWidth < 360.dp -> ScreenSize.SMALL
        screenWidth < 600.dp -> ScreenSize.COMPACT
        screenWidth < 840.dp -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

// Responsive dimensions
object ResponsiveDimensions {
    
    @Composable
    fun cardPadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.dp
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.EXPANDED -> 24.dp
        }
    }
    
    @Composable
    fun screenPadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.dp
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.EXPANDED -> 24.dp
        }
    }
    
    @Composable
    fun buttonHeight(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 44.dp
            ScreenSize.COMPACT -> 48.dp
            ScreenSize.MEDIUM -> 52.dp
            ScreenSize.EXPANDED -> 56.dp
        }
    }
    
    @Composable
    fun iconSize(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 20.dp
            ScreenSize.COMPACT -> 24.dp
            ScreenSize.MEDIUM -> 28.dp
            ScreenSize.EXPANDED -> 32.dp
        }
    }
    
    @Composable
    fun cardCornerRadius(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.dp
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 20.dp
            ScreenSize.EXPANDED -> 24.dp
        }
    }
    
    @Composable
    fun spacingSmall(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 4.dp
            ScreenSize.COMPACT -> 8.dp
            ScreenSize.MEDIUM -> 12.dp
            ScreenSize.EXPANDED -> 16.dp
        }
    }
    
    @Composable
    fun spacingMedium(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 8.dp
            ScreenSize.COMPACT -> 12.dp
            ScreenSize.MEDIUM -> 16.dp
            ScreenSize.EXPANDED -> 20.dp
        }
    }
    
    @Composable
    fun spacingLarge(): Dp {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.dp
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.EXPANDED -> 32.dp
        }
    }
}

// Responsive text sizes
object ResponsiveText {
    
    @Composable
    fun titleLarge(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 20.sp
            ScreenSize.COMPACT -> 24.sp
            ScreenSize.MEDIUM -> 28.sp
            ScreenSize.EXPANDED -> 32.sp
        }
    }
    
    @Composable
    fun titleMedium(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 16.sp
            ScreenSize.COMPACT -> 18.sp
            ScreenSize.MEDIUM -> 20.sp
            ScreenSize.EXPANDED -> 22.sp
        }
    }
    
    @Composable
    fun titleSmall(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 14.sp
            ScreenSize.COMPACT -> 16.sp
            ScreenSize.MEDIUM -> 18.sp
            ScreenSize.EXPANDED -> 20.sp
        }
    }
    
    @Composable
    fun bodyLarge(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 14.sp
            ScreenSize.COMPACT -> 16.sp
            ScreenSize.MEDIUM -> 18.sp
            ScreenSize.EXPANDED -> 20.sp
        }
    }
    
    @Composable
    fun bodyMedium(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 12.sp
            ScreenSize.COMPACT -> 14.sp
            ScreenSize.MEDIUM -> 16.sp
            ScreenSize.EXPANDED -> 18.sp
        }
    }
    
    @Composable
    fun bodySmall(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 10.sp
            ScreenSize.COMPACT -> 12.sp
            ScreenSize.MEDIUM -> 14.sp
            ScreenSize.EXPANDED -> 16.sp
        }
    }
    
    @Composable
    fun caption(): TextUnit {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 10.sp
            ScreenSize.COMPACT -> 11.sp
            ScreenSize.MEDIUM -> 12.sp
            ScreenSize.EXPANDED -> 13.sp
        }
    }
}

// Responsive grid columns
object ResponsiveGrid {
    
    @Composable
    fun columns(): Int {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 1      // 1 column for small phones
            ScreenSize.COMPACT -> 2    // 2 columns for normal phones
            ScreenSize.MEDIUM -> 3     // 3 columns for large phones
            ScreenSize.EXPANDED -> 4   // 4 columns for tablets
        }
    }
    
    @Composable
    fun dashboardColumns(): Int {
        return when (getScreenSize()) {
            ScreenSize.SMALL -> 2      // 2 columns for small phones
            ScreenSize.COMPACT -> 2    // 2 columns for normal phones
            ScreenSize.MEDIUM -> 3     // 3 columns for large phones
            ScreenSize.EXPANDED -> 4   // 4 columns for tablets
        }
    }
}

// Screen width and height helpers
@Composable
fun screenWidth(): Dp {
    return LocalConfiguration.current.screenWidthDp.dp
}

@Composable
fun screenHeight(): Dp {
    return LocalConfiguration.current.screenHeightDp.dp
}

// Check if device is tablet
@Composable
fun isTablet(): Boolean {
    return getScreenSize() in listOf(ScreenSize.MEDIUM, ScreenSize.EXPANDED)
}

// Check if device is small phone
@Composable
fun isSmallPhone(): Boolean {
    return getScreenSize() == ScreenSize.SMALL
}

// Responsive width percentage
@Composable
fun widthPercent(percent: Float): Dp {
    return screenWidth() * percent
}

// Responsive height percentage
@Composable
fun heightPercent(percent: Float): Dp {
    return screenHeight() * percent
}

// Minimum touch target size (44dp for accessibility)
val MinTouchTarget = 44.dp

// Maximum content width for readability
@Composable
fun maxContentWidth(): Dp {
    return when (getScreenSize()) {
        ScreenSize.SMALL -> screenWidth()
        ScreenSize.COMPACT -> screenWidth()
        ScreenSize.MEDIUM -> 600.dp
        ScreenSize.EXPANDED -> 840.dp
    }
}
