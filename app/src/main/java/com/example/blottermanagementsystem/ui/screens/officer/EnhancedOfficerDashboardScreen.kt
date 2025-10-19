package com.example.blottermanagementsystem.ui.screens.officer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.ui.components.DashboardTopBar
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.ExportUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedOfficerDashboardScreen(
    officerName: String,
    officerId: Int,
    onNavigateToAssignedCases: () -> Unit,
    onNavigateToHearings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReportDetail: (Int) -> Unit,
    onNavigateToQRScanner: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val assignedReports by viewModel.getReportsByOfficerId(officerId).collectAsState(initial = emptyList())
    
    var isExporting by remember { mutableStateOf(false) }
    
    val pendingCount = assignedReports.count { it.status == "Pending" || it.status == "Assigned" }
    val ongoingCount = assignedReports.count { it.status == "Under Investigation" }
    val resolvedCount = assignedReports.count { it.status == "Resolved" }
    val urgentCases = assignedReports.filter { 
        (it.status == "Pending" || it.status == "Assigned") && (System.currentTimeMillis() - it.dateFiled) > 86400000 // > 24 hours
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Officer Dashboard",
                subtitle = "Case Management",
                firstName = officerName,
                notificationCount = urgentCases.size,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated "Overview" Header with boundary lines
            item {
                Spacer(modifier = Modifier.height(8.dp))
                AnimatedOverviewHeader()
            }
            
            // Performance Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GradientStatCard(
                        title = "Total Cases",
                        value = assignedReports.size.toString(),
                        icon = Icons.Default.List,
                        gradient = Brush.linearGradient(
                            colors = listOf(ElectricBlue, InfoBlue)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    GradientStatCard(
                        title = "Resolved",
                        value = resolvedCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        gradient = Brush.linearGradient(
                            colors = listOf(SuccessGreen, Color(0xFF00C853))
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GradientStatCard(
                        title = "Pending",
                        value = pendingCount.toString(),
                        icon = Icons.Default.Warning,
                        gradient = Brush.linearGradient(
                            colors = listOf(WarningOrange, Color(0xFFFF6F00))
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    GradientStatCard(
                        title = "Ongoing",
                        value = ongoingCount.toString(),
                        icon = Icons.Default.Refresh,
                        gradient = Brush.linearGradient(
                            colors = listOf(InfoBlue, Color(0xFF0091EA))
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Urgent Cases Alert
            if (urgentCases.isNotEmpty()) {
                item {
                    UrgentCasesCard(
                        count = urgentCases.size,
                        onClick = onNavigateToAssignedCases
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
                    OfficerActionCard(
                        title = "My Cases",
                        icon = Icons.Default.List,
                        onClick = onNavigateToAssignedCases,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    OfficerActionCard(
                        title = "Hearings",
                        icon = Icons.Default.DateRange,
                        onClick = onNavigateToHearings,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OfficerActionCard(
                        title = "Scan QR Code",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = onNavigateToQRScanner,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    OfficerActionCard(
                        title = "Analytics",
                        icon = Icons.Default.BarChart,
                        onClick = onNavigateToAnalytics,
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Export My Cases
            item {
                OfficerActionCard(
                    title = "Export My Cases (Excel)",
                    icon = Icons.Default.FileDownload,
                    onClick = {
                        if (!isExporting && assignedReports.isNotEmpty()) {
                            isExporting = true
                            scope.launch {
                                ExportUtils.exportCasesToExcel(
                                    context = context,
                                    reports = assignedReports,
                                    onSuccess = { uri ->
                                        isExporting = false
                                        ExportUtils.shareExportedFile(context, uri, "My_Cases_${officerName}.xlsx")
                                        Toast.makeText(context, "Cases exported successfully!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        isExporting = false
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        } else if (assignedReports.isEmpty()) {
                            Toast.makeText(context, "No cases to export", Toast.LENGTH_SHORT).show()
                        }
                    },
                    color = SuccessGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Recent Cases
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Cases",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToAssignedCases) {
                        Text("View All", color = ElectricBlue)
                    }
                }
            }
            
            if (assignedReports.isEmpty()) {
                item {
                    EmptyCasesCard()
                }
            } else {
                items(assignedReports.take(5)) { report ->
                    com.example.blottermanagementsystem.ui.screens.dashboard.RecentReportCard(
                        report = report,
                        onClick = { onNavigateToReportDetail(report.id) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun AnimatedOverviewHeader() {
    var lineProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic)
        ) { value, _ ->
            lineProgress = value
        }
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Animated lines
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        ) {
            val lineWidth = (size.width - 220.dp.toPx()) / 2
            
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
        
        // "OVERVIEW" text
        Text(
            text = "OVERVIEW",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = ElectricBlue,
            letterSpacing = 4.sp,
            modifier = Modifier
                .background(DarkNavy)
                .padding(horizontal = 20.dp)
        )
    }
}

@Composable
fun GradientStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = value,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun UrgentCasesCard(count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRed.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Urgent Attention Required",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
                Text(
                    text = "$count case${if (count > 1) "s" else ""} pending for over 24 hours",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = ErrorRed
            )
        }
    }
}

@Composable
fun OfficerActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyCasesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No cases assigned yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cases will appear here when assigned by admin",
                fontSize = 13.sp,
                color = TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
