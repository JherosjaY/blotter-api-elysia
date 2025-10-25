package com.example.blottermanagementsystem.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Auto-navigate after animation
    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
        delay(2500) // Show splash for 2.5 seconds
        onNavigateToNext()
    }
    
    // OPTIMIZED: Single infinite transition for all animations
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Particle animations
    val particle1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1"
    )
    
    val particle2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -80f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2"
    )
    
    val particle3Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle3"
    )
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Entry animations - OPTIMIZED with single animateFloatAsState
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, delayMillis = 500),
        label = "textAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0f172a),
                        Color(0xFF1e293b),
                        Color(0xFF0f172a)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // OPTIMIZED PARTICLES - Fewer, larger blur radius
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = particle1Y.dp)
                .alpha(glowPulse * 0.4f)
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopEnd)
                .offset(x = 120.dp, y = particle2Y.dp)
                .alpha(glowPulse * 0.35f)
                .blur(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            InfoBlue.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 150.dp)
                .rotate(particle3Rotation)
                .alpha(glowPulse * 0.3f)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF60a5fa).copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // MAIN CONTENT
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ANIMATED LOGO
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .alpha(glowPulse * 0.7f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Middle glow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .alpha(glowPulse * 0.8f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    InfoBlue.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                
                // Logo container
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = 0.3f),
                                    InfoBlue.copy(alpha = 0.3f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔐",
                        fontSize = 72.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // APP NAME with fade in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha)
            ) {
                Text(
                    text = "Blotter Management",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "System",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Loading indicator
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ElectricBlue.copy(alpha = glowPulse),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
