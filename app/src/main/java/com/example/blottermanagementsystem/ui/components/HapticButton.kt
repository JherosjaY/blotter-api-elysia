package com.example.blottermanagementsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.blottermanagementsystem.utils.rememberHapticFeedback

/**
 * Button with haptic feedback
 */
@Composable
fun HapticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    Button(
        onClick = {
            if (enabled) {
                haptic.lightTap()
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Outlined Button with haptic feedback
 */
@Composable
fun HapticOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    OutlinedButton(
        onClick = {
            if (enabled) {
                haptic.lightTap()
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Text Button with haptic feedback
 */
@Composable
fun HapticTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    TextButton(
        onClick = {
            if (enabled) {
                haptic.lightTap()
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

/**
 * Icon Button with haptic feedback
 */
@Composable
fun HapticIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    IconButton(
        onClick = {
            if (enabled) {
                haptic.lightTap()
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

/**
 * Card with haptic feedback on click
 */
@Composable
fun HapticCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = rememberHapticFeedback()
    
    Card(
        onClick = {
            if (enabled) {
                haptic.lightTap()
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        content = content
    )
}

/**
 * Clickable modifier with haptic feedback
 */
@Composable
fun Modifier.hapticClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val haptic = rememberHapticFeedback()
    return this.clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        if (enabled) {
            haptic.lightTap()
            onClick()
        }
    }
}

/**
 * Switch with haptic feedback
 */
@Composable
fun HapticSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    val haptic = rememberHapticFeedback()
    
    Switch(
        checked = checked,
        onCheckedChange = { newValue ->
            if (enabled) {
                haptic.lightTap()
                onCheckedChange(newValue)
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

/**
 * Checkbox with haptic feedback
 */
@Composable
fun HapticCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    val haptic = rememberHapticFeedback()
    
    Checkbox(
        checked = checked,
        onCheckedChange = { newValue ->
            if (enabled) {
                haptic.lightTap()
                onCheckedChange(newValue)
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

/**
 * Radio Button with haptic feedback
 */
@Composable
fun HapticRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors()
) {
    val haptic = rememberHapticFeedback()
    
    RadioButton(
        selected = selected,
        onClick = {
            if (enabled) {
                haptic.lightTap()
                onClick()
            }
        },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}
