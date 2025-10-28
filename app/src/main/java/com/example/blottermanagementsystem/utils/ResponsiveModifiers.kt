package com.example.blottermanagementsystem.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * ResponsiveModifiers - Easy-to-use responsive modifiers
 * Automatically adapts to screen size
 */

// Responsive padding
@Composable
fun Modifier.responsivePadding(): Modifier {
    return this.padding(ResponsiveDimensions.screenPadding())
}

@Composable
fun Modifier.responsiveCardPadding(): Modifier {
    return this.padding(ResponsiveDimensions.cardPadding())
}

// Responsive width with max constraint
@Composable
fun Modifier.responsiveWidth(): Modifier {
    return this
        .fillMaxWidth()
        .widthIn(max = maxContentWidth())
}

// Responsive spacing
@Composable
fun Modifier.responsiveSpacingSmall(): Modifier {
    return this.padding(ResponsiveDimensions.spacingSmall())
}

@Composable
fun Modifier.responsiveSpacingMedium(): Modifier {
    return this.padding(ResponsiveDimensions.spacingMedium())
}

@Composable
fun Modifier.responsiveSpacingLarge(): Modifier {
    return this.padding(ResponsiveDimensions.spacingLarge())
}

// Responsive horizontal padding
@Composable
fun Modifier.responsiveHorizontalPadding(): Modifier {
    return this.padding(horizontal = ResponsiveDimensions.screenPadding())
}

// Responsive vertical padding
@Composable
fun Modifier.responsiveVerticalPadding(): Modifier {
    return this.padding(vertical = ResponsiveDimensions.screenPadding())
}
