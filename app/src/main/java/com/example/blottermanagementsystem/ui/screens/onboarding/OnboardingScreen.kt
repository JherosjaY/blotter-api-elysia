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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkNavy, DarkNavyLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Pager
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Page indicators
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = ElectricBlue,
                inactiveColor = TextTertiary,
                indicatorWidth = 12.dp,
                indicatorHeight = 12.dp,
                spacing = 8.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Next/Get Started button
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
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (pagerState.currentPage == pages.size - 1) {
                    Text(
                        text = "🚀",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                } else {
                    Text(
                        text = "Next",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    // Bounce animation for icon only
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circle with shadow (no animation on circle)
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = ElectricBlue.copy(alpha = 0.5f),
                    spotColor = ElectricBlue.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.3f),
                            DarkNavyLight
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Icon with bounce animation
            Text(
                text = page.icon,
                fontSize = 100.sp,
                modifier = Modifier.scale(iconScale)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
