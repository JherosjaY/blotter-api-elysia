package com.example.blottermanagementsystem.ui.screens.dashboard

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.blottermanagementsystem.ui.components.DashboardTopBar
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.ExportUtils
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    firstName: String = "Admin",
    onNavigateToReports: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToOfficers: () -> Unit,
    onNavigateToActivityLogs: () -> Unit,
    onNavigateToReportOversight: () -> Unit = {},
    onNavigateToHearings: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val totalReports by viewModel.allReports.collectAsState(initial = emptyList())
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    
    val pendingCount = totalReports.count { it.status == "Pending" || it.status == "Assigned" }
    val ongoingCount = totalReports.count { it.status == "Under Investigation" }
    val resolvedCount = totalReports.count { it.status == "Resolved" }
    val totalCount = totalReports.size
    
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Admin Dashboard",
                subtitle = "System Overview",
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Gauge Charts Section
            item {
                Text(
                    text = "Reports Overview",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pending Gauge
                    GaugeCard(
                        title = "Pending",
                        value = pendingCount,
                        maxValue = totalCount.coerceAtLeast(1),
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Ongoing Gauge
                    GaugeCard(
                        title = "Investigating",
                        value = ongoingCount,
                        maxValue = totalCount.coerceAtLeast(1),
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                // Resolved Gauge (Full Width)
                GaugeCard(
                    title = "Resolved Cases",
                    value = resolvedCount,
                    maxValue = totalCount.coerceAtLeast(1),
                    color = SuccessGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Gradient Stats Cards
            item {
                Text(
                    text = "System Statistics",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Centered grid layout (Windows 11 taskbar style)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    GradientStatCard(
                        title = "Total Reports",
                        value = totalCount.toString(),
                        icon = Icons.Default.List,
                        gradient = Brush.linearGradient(
                            colors = listOf(ElectricBlue, InfoBlue)
                        ),
                        modifier = Modifier.width(160.dp)
                    )
                    
                    GradientStatCard(
                        title = "Officers",
                        value = dashboardStats.totalOfficers.toString(),
                        icon = Icons.Default.AccountCircle,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                        ),
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    GradientStatCard(
                        title = "Users",
                        value = dashboardStats.totalUsers.toString(),
                        icon = Icons.Default.Person,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
                        ),
                        modifier = Modifier.width(160.dp)
                    )
                    
                    GradientStatCard(
                        title = "Archived",
                        value = dashboardStats.archivedReports.toString(),
                        icon = Icons.Default.Archive,
                        gradient = Brush.linearGradient(
                            colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
                        ),
                        modifier = Modifier.width(160.dp)
                    )
                }
            }
            
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Manage Users",
                        icon = Icons.Default.Person,
                        onClick = onNavigateToUsers,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Manage Officers",
                        icon = Icons.Default.AccountCircle,
                        onClick = onNavigateToOfficers,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Report Oversight",
                        icon = Icons.Default.Visibility,
                        onClick = onNavigateToReportOversight,
                        modifier = Modifier.weight(1f),
                        containerColor = InfoBlue
                    )
                    ActionCard(
                        title = "Activity Logs",
                        icon = Icons.Default.History,
                        onClick = onNavigateToActivityLogs,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Hearings",
                        icon = Icons.Default.Event,
                        onClick = onNavigateToHearings,
                        modifier = Modifier.weight(1f),
                        containerColor = WarningYellow
                    )
                    ActionCard(
                        title = "Analytics",
                        icon = Icons.Default.Analytics,
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f),
                        containerColor = InfoBlue
                    )
                }
            }
            
            item {
                ActionCard(
                    title = "View Reports",
                    icon = Icons.Default.List,
                    onClick = onNavigateToReports,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Export Reports Section
            item {
                Text(
                    text = "Export Reports",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title = "Export All Cases",
                        icon = Icons.Default.TableChart,
                        onClick = {
                            if (!isExporting && totalReports.isNotEmpty()) {
                                isExporting = true
                                scope.launch {
                                    try {
                                        ExportUtils.exportCasesToExcel(
                                            context = context,
                                            reports = totalReports,
                                            onSuccess = { uri ->
                                                isExporting = false
                                                ExportUtils.shareExportedFile(context, uri, "Blotter_Reports.xlsx")
                                                Toast.makeText(context, "Cases exported successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { error ->
                                                isExporting = false
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } catch (e: Exception) {
                                        isExporting = false
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else if (totalReports.isEmpty()) {
                                Toast.makeText(context, "No cases to export", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = SuccessGreen
                    )
                    ActionCard(
                        title = "Export Statistics",
                        icon = Icons.Default.BarChart,
                        onClick = {
                            if (!isExporting && totalReports.isNotEmpty()) {
                                isExporting = true
                                scope.launch {
                                    try {
                                        val casesByType = totalReports.groupBy { it.incidentType }.mapValues { it.value.size }
                                        val casesByStatus = totalReports.groupBy { it.status }.mapValues { it.value.size }
                                        
                                        ExportUtils.exportStatisticsToExcel(
                                            context = context,
                                            totalCases = totalCount,
                                            activeCases = pendingCount + ongoingCount,
                                            resolvedCases = resolvedCount,
                                            pendingCases = pendingCount,
                                            casesByType = casesByType,
                                            casesByStatus = casesByStatus,
                                            onSuccess = { uri ->
                                                isExporting = false
                                                ExportUtils.shareExportedFile(context, uri, "Statistics.xlsx")
                                                Toast.makeText(context, "Statistics exported successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { error ->
                                                isExporting = false
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } catch (e: Exception) {
                                        isExporting = false
                                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else if (totalReports.isEmpty()) {
                                Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = InfoBlue
                    )
                }
            }
            
            item {
                ActionCard(
                    title = "Export Officer Performance",
                    icon = Icons.Default.Assessment,
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            scope.launch {
                                try {
                                    val officers = viewModel.getAllOfficersSync()
                                    
                                    if (officers.isEmpty()) {
                                        isExporting = false
                                        Toast.makeText(context, "No officers to export", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    
                                    val officerCaseCounts = mutableMapOf<Int, Int>()
                                    val officerResolvedCounts = mutableMapOf<Int, Int>()
                                    
                                    officers.forEach { officer ->
                                        val assignedCases = totalReports.filter { 
                                            it.assignedOfficerIds?.contains(officer.id.toString()) == true 
                                        }
                                        officerCaseCounts[officer.id] = assignedCases.size
                                        officerResolvedCounts[officer.id] = assignedCases.count { it.status == "Resolved" }
                                    }
                                    
                                    ExportUtils.exportOfficerPerformanceToExcel(
                                        context = context,
                                        officers = officers,
                                        officerCaseCounts = officerCaseCounts,
                                        officerResolvedCounts = officerResolvedCounts,
                                        onSuccess = { uri ->
                                            isExporting = false
                                            ExportUtils.shareExportedFile(context, uri, "Officer_Performance.xlsx")
                                            Toast.makeText(context, "Officer performance exported successfully!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            isExporting = false
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                } catch (e: Exception) {
                                    isExporting = false
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = WarningOrange
                )
            }
            
            // Recent Reports Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Reports",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToReports) {
                        Text("View All", color = ElectricBlue)
                    }
                }
            }
            
            if (totalReports.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
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
                                text = "No reports yet",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Reports will appear here when filed",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(totalReports.take(5)) { report ->
                    RecentReportCard(
                        report = report,
                        onClick = onNavigateToReports
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
fun RecentReportCard(
    report: com.example.blottermanagementsystem.data.entity.BlotterReport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.caseNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                Surface(
                    color = when (report.status) {
                        "Pending" -> WarningOrange.copy(alpha = 0.2f)
                        "Under Investigation" -> InfoBlue.copy(alpha = 0.2f)
                        "Resolved" -> SuccessGreen.copy(alpha = 0.2f)
                        else -> TextSecondary.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = report.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (report.status) {
                            "Pending" -> WarningOrange
                            "Under Investigation" -> InfoBlue
                            "Resolved" -> SuccessGreen
                            else -> TextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Complainant: ${report.complainantName}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Type: ${report.incidentType}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(report.dateFiled)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Gauge Chart Card
@Composable
fun GaugeCard(
    title: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val percentage = (value.toFloat() / maxValue.toFloat() * 100).coerceIn(0f, 100f)
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "gauge"
    )
    
    Card(
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                // Gauge Chart
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 20f
                    val sweepAngle = 180f
                    val startAngle = 180f
                    
                    // Background arc
                    drawArc(
                        color = color.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                    
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * (animatedPercentage / 100f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                }
                
                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 10.dp)
                ) {
                    Text(
                        text = value.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "${animatedPercentage.toInt()}%",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Gradient Stat Card
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
        shape = RoundedCornerShape(20.dp),
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
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// Action Card
@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CardBackground
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
