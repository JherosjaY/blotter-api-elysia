package com.example.blottermanagementsystem.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    onLoadingComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Trigger animation on launch
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Show loading for 2.5 seconds
        onLoadingComplete()
    }
    
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Fade in animation
    val fadeIn by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(800),
        label = "fadeIn"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkNavy,
                        DarkNavyLight,
                        DarkNavy
                    )
                )
            )
            .alpha(fadeIn),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo/Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.6f),
                                ElectricBlue.copy(alpha = 0.2f),
                                DarkNavyLight
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔐",
                    fontSize = 64.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Name
            Text(
                text = "Blotter Management",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "System",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading Text
            Text(
                text = "Loading...",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Loading Dots Animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 600,
                                delayMillis = index * 200,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(dotAlpha)
                            .background(ElectricBlue, CircleShape)
                    )
                }
            }
        }
    }
}
