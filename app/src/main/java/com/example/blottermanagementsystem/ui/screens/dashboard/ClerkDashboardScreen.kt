package com.example.blottermanagementsystem.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.components.DashboardTopBar
import com.example.blottermanagementsystem.ui.components.EmptyState
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClerkDashboardScreen(
    userId: Int,
    firstName: String = "User",
    onNavigateToAddReport: () -> Unit,
    onNavigateToViewReports: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPendingReports: () -> Unit,
    onNavigateToHearings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    // Filter reports by current user
    val myReports = allReports.filter { it.userId == userId }
    val pendingCount = myReports.count { it.status == "Pending" || it.status == "Assigned" }
    val ongoingCount = myReports.count { it.status == "Under Investigation" }
    val resolvedCount = myReports.count { it.status == "Resolved" }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Clerk Dashboard",
                subtitle = "Data Entry & Management",
                firstName = firstName,
                notificationCount = 0,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp  // Minimal bottom space
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = true
        ) {
            // Stats Overview
            item {
                Text(
                    text = "Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Reports",
                        value = myReports.size.toString(),
                        icon = Icons.Default.List,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending",
                        value = pendingCount.toString(),
                        icon = Icons.Default.Warning,
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Ongoing",
                        value = ongoingCount.toString(),
                        icon = Icons.Default.Refresh,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Resolved",
                        value = resolvedCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClerkActionCard(
                        title = "Add New Report",
                        description = "Create a new blotter report",
                        icon = Icons.Default.AddCircle,
                        onClick = onNavigateToAddReport,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    ClerkActionCard(
                        title = "View Reports",
                        description = "Browse all reports",
                        icon = Icons.Default.List,
                        onClick = onNavigateToViewReports,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                    ClerkActionCard(
                        title = "Hearings",
                        description = "View all hearings",
                        icon = Icons.Default.Event,
                        onClick = onNavigateToHearings,
                        color = WarningYellow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // My Recent Reports
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Recent Reports",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToPendingReports) {
                        Text(
                            text = "View All",
                            color = ElectricBlue
                        )
                    }
                }
            }
            
            if (myReports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No reports created yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click the + button to create your first report",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(myReports.take(5)) { report ->
                    com.example.blottermanagementsystem.ui.screens.dashboard.RecentReportCard(
                        report = report,
                        onClick = onNavigateToViewReports
                    )
                }
            }
            
            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = InfoBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Note",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You have read-only access to reports created by other users.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Animated shimmer effect - MORE GLOW!
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    val glowSize by infiniteTransition.animateFloat(
        initialValue = 120f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowSize"
    )
    
    Card(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.15f)
                        )
                    )
                )
        ) {
            // Multiple animated glow circles - MORE GLOW!
            Box(
                modifier = Modifier
                    .size(glowSize.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .alpha(shimmerAlpha * 0.6f)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.6f),
                                color.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-30).dp, y = 30.dp)
                    .alpha(shimmerAlpha * 0.4f)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Content - Centered with icon and number side by side
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon and Number side by side
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon with glow effect
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = color.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Number
                    Text(
                        text = value,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        letterSpacing = (-2).sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ClerkActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 16.sp
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
