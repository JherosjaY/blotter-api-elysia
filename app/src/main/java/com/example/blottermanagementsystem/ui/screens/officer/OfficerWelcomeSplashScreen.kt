package com.example.blottermanagementsystem.ui.screens.officer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OfficerWelcomeSplashScreen(
    officerName: String,
    onAnimationComplete: () -> Unit
) {
    var animationPhase by remember { mutableStateOf(0) }
    
    // Phase 0: Logo zoom in (0-1000ms)
    // Phase 1: Logo zoom out (1000-2000ms)
    // Phase 2: Show overview (2000-3000ms)
    
    val logoScale by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 3f // Start large
            1 -> 1f // Zoom to normal
            else -> 0.5f // Shrink to top
        },
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "logoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (animationPhase < 2) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "logoAlpha"
    )
    
    val overviewAlpha by animateFloatAsState(
        targetValue = if (animationPhase >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "overviewAlpha"
    )
    
    val lineProgress by animateFloatAsState(
        targetValue = if (animationPhase >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "lineProgress"
    )
    
    LaunchedEffect(Unit) {
        delay(1000) // Logo zoom in
        animationPhase = 1
        delay(1000) // Logo zoom out
        animationPhase = 2
        delay(1500) // Show overview
        onAnimationComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy),
        contentAlignment = Alignment.Center
    ) {
        // Police Badge Logo (Center during animation)
        if (animationPhase < 2) {
            PoliceBadgeLogo(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )
        }
        
        // Overview Section (After animation)
        if (animationPhase >= 2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(overviewAlpha)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated boundary lines + "Overview" text
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Left and Right lines
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    ) {
                        val lineWidth = (size.width - 200.dp.toPx()) / 2
                        
                        // Left line
                        drawLine(
                            color = ElectricBlue,
                            start = Offset(0f, size.height / 2),
                            end = Offset(lineWidth * lineProgress, size.height / 2),
                            strokeWidth = 4f
                        )
                        
                        // Right line
                        drawLine(
                            color = ElectricBlue,
                            start = Offset(size.width, size.height / 2),
                            end = Offset(size.width - (lineWidth * lineProgress), size.height / 2),
                            strokeWidth = 4f
                        )
                    }
                    
                    // "Overview" text
                    Text(
                        text = "OVERVIEW",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 4.sp,
                        modifier = Modifier
                            .background(DarkNavy)
                            .padding(horizontal = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Welcome message
                Text(
                    text = "Welcome, Officer $officerName",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun PoliceBadgeLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2.5f
            
            // Outer shield shape (7-pointed star badge)
            val path = Path().apply {
                val points = 7
                val outerRadius = radius
                val innerRadius = radius * 0.6f
                
                for (i in 0 until points * 2) {
                    val angle = (Math.PI / points * i - Math.PI / 2).toFloat()
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val x = centerX + r * kotlin.math.cos(angle)
                    val y = centerY + r * kotlin.math.sin(angle)
                    
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            
            // Draw badge background
            drawPath(
                path = path,
                color = ElectricBlue.copy(alpha = 0.3f)
            )
            
            // Draw badge outline
            drawPath(
                path = path,
                color = ElectricBlue,
                style = Stroke(width = 8f)
            )
            
            // Inner circle
            drawCircle(
                color = DarkNavy,
                radius = radius * 0.5f,
                center = Offset(centerX, centerY)
            )
            
            drawCircle(
                color = ElectricBlue,
                radius = radius * 0.5f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 6f)
            )
            
            // Center star
            val starPath = Path().apply {
                val starPoints = 5
                val starOuterRadius = radius * 0.35f
                val starInnerRadius = radius * 0.15f
                
                for (i in 0 until starPoints * 2) {
                    val angle = (Math.PI / starPoints * i - Math.PI / 2).toFloat()
                    val r = if (i % 2 == 0) starOuterRadius else starInnerRadius
                    val x = centerX + r * kotlin.math.cos(angle)
                    val y = centerY + r * kotlin.math.sin(angle)
                    
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            
            drawPath(
                path = starPath,
                color = WarningOrange
            )
        }
        
        // "POLICE" text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 80.dp)
        ) {
            Text(
                text = "POLICE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ElectricBlue,
                letterSpacing = 2.sp
            )
        }
    }
}
