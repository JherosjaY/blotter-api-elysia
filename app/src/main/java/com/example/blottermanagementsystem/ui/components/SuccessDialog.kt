package com.example.blottermanagementsystem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.blottermanagementsystem.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SuccessDialog(
    title: String = "Success!",
    message: String,
    onDismiss: () -> Unit,
    autoDismissDelay: Long = 2000L
) {
    var isVisible by remember { mutableStateOf(true) }
    
    // Auto dismiss after delay
    LaunchedEffect(Unit) {
        delay(autoDismissDelay)
        isVisible = false
        delay(300) // Wait for animation
        onDismiss()
    }
    
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            SuccessDialogContent(
                title = title,
                message = message
            )
        }
    }
}

@Composable
private fun SuccessDialogContent(
    title: String,
    message: String
) {
    // Animation for scale
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Icon with animation
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Title
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            // Message
            Text(
                text = message,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) ErrorRed else ElectricBlue
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText, color = TextSecondary)
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle, // Use Error icon if available
                contentDescription = "Error",
                tint = ErrorRed,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                )
            ) {
                Text("OK")
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun LoadingDialog(
    message: String = "Loading..."
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = ElectricBlue
                )
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }
        }
    }
}
