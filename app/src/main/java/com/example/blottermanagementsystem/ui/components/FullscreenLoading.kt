package com.example.blottermanagementsystem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.blottermanagementsystem.ui.theme.DarkNavy
import com.example.blottermanagementsystem.ui.theme.ElectricBlue

@Composable
fun FullscreenLoadingDialog(
    message: String = "Saving...",
    show: Boolean = true
) {
    if (show) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkNavy.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated CircularProgressIndicator
                    CircularProgressIndicator(
                        modifier = Modifier.size(80.dp),
                        color = ElectricBlue,
                        strokeWidth = 6.dp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Animated text
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    
                    Text(
                        text = message,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}
