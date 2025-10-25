package com.example.blottermanagementsystem.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: String
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    
    val pages = listOf(
        OnboardingPage(
            title = "Manage Blotter Reports Efficiently",
            description = "Streamline your case management with our comprehensive blotter system. Track, update, and manage all reports in one place.",
            icon = "📋"
        ),
        OnboardingPage(
            title = "Record Suspects, Witnesses, and Evidence",
            description = "Easily document all case details including suspects, witnesses, and evidence with organized data entry.",
            icon = "🔍"
        ),
        OnboardingPage(
            title = "Track Case Progress and Hearings",
            description = "Monitor case status updates and schedule hearings with automated notifications and reminders.",
            icon = "📊"
        ),
        OnboardingPage(
            title = "Stay Notified Anytime, Anywhere",
            description = "Receive real-time notifications about case updates, hearing schedules, and important events.",
            icon = "🔔"
        )
    )

    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    
    // OPTIMIZED: Single infinite transition for background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val particle1Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle1"
    )
    
    val particle2Y by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle2"
    )
    
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
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
            )
    ) {
        // OPTIMIZED PARTICLES - Fewer, larger blur
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = particle1Y.dp)
                .alpha(glowPulse * 0.4f)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = particle2Y.dp)
                .alpha(glowPulse * 0.35f)
                .blur(70.dp)
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Pager with glassmorphism cards
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    currentPage = pagerState.currentPage,
                    pageIndex = page
                )
            }

            // MODERN PAGE INDICATORS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (isSelected) 32.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // GLASSMORPHISM BUTTON
            Button(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage == pages.size - 1) {
                            preferencesManager.onboardingCompleted = true
                            onFinish()
                        } else {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                if (pagerState.currentPage == pages.size - 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚀",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Get Started",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    currentPage: Int,
    pageIndex: Int
) {
    // OPTIMIZED: Only animate when page is visible
    val isVisible = currentPage == pageIndex
    
    val iconScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "iconScale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = tween(500),
        label = "contentAlpha"
    )
    
    // OPTIMIZED: Subtle bounce only when visible
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val iconBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isVisible) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .alpha(contentAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // GLASSMORPHISM CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(iconScale),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with glow
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .offset(y = iconBounce.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .alpha(0.5f)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            ElectricBlue.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        // Icon container
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            ElectricBlue.copy(alpha = 0.2f),
                                            InfoBlue.copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = page.icon,
                                fontSize = 64.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Title
                    Text(
                        text = page.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = page.description,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
