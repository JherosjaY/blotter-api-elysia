package com.example.blottermanagementsystem.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    
    // Calculate advanced metrics
    val totalCases = allReports.size
    val resolvedCases = allReports.count { it.status == "Resolved" }
    val pendingCases = allReports.count { it.status == "Pending" }
    val underInvestigation = allReports.count { it.status == "Under Investigation" }
    
    val resolutionRate = if (totalCases > 0) (resolvedCases.toFloat() / totalCases * 100).toInt() else 0
    
    // Case types breakdown
    val casesByType = allReports.groupBy { it.incidentType }.mapValues { it.value.size }
    val topIncidentType = casesByType.maxByOrNull { it.value }
    
    // Monthly trend (last 6 months)
    val currentTime = System.currentTimeMillis()
    val sixMonthsAgo = currentTime - (180L * 24 * 60 * 60 * 1000)
    val recentCases = allReports.filter { it.dateFiled >= sixMonthsAgo }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Analytics Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Export analytics */ }) {
                        Icon(Icons.Default.Download, "Export", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Section
            item {
                Text(
                    "Performance Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Cases",
                        value = totalCases.toString(),
                        icon = Icons.Default.Assessment,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Resolution Rate",
                        value = "$resolutionRate%",
                        icon = Icons.Default.CheckCircle,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Pending",
                        value = pendingCases.toString(),
                        icon = Icons.Default.HourglassEmpty,
                        color = WarningOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Investigating",
                        value = underInvestigation.toString(),
                        icon = Icons.Default.Search,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Case Distribution
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Case Distribution",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        casesByType.entries.sortedByDescending { it.value }.take(5).forEach { (type, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        type,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "$count cases",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Progress bar
                                val percentage = (count.toFloat() / totalCases * 100).toInt()
                                LinearProgressIndicator(
                                    progress = count.toFloat() / totalCases,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(8.dp),
                                    color = ElectricBlue,
                                    trackColor = ElectricBlue.copy(alpha = 0.2f)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    "$percentage%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            }
                            
                            if (type != casesByType.entries.sortedByDescending { it.value }.take(5).last().key) {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
            
            // Insights
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Key Insights",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                InsightCard(
                    icon = Icons.Default.TrendingUp,
                    title = "Most Common Case Type",
                    description = "${topIncidentType?.key ?: "N/A"} with ${topIncidentType?.value ?: 0} cases",
                    color = ElectricBlue
                )
            }
            
            item {
                InsightCard(
                    icon = Icons.Default.Speed,
                    title = "Recent Activity",
                    description = "${ recentCases.size} cases filed in the last 6 months",
                    color = SuccessGreen
                )
            }
            
            item {
                InsightCard(
                    icon = Icons.Default.Warning,
                    title = "Attention Needed",
                    description = "$pendingCases cases pending review",
                    color = WarningOrange
                )
            }
            
            // Recommendations
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Recommendations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = InfoBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = InfoBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "System Recommendations",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (pendingCases > 10) {
                            Text(
                                "• Consider assigning more officers to pending cases",
                                fontSize = 13.sp
                            )
                        }
                        if (resolutionRate < 50) {
                            Text(
                                "• Resolution rate is below 50% - review investigation processes",
                                fontSize = 13.sp
                            )
                        }
                        if (topIncidentType != null && topIncidentType.value > totalCases * 0.3) {
                            Text(
                                "• ${topIncidentType.key} cases are increasing - consider preventive measures",
                                fontSize = 13.sp
                            )
                        }
                        if (pendingCases <= 5 && resolutionRate >= 70) {
                            Text(
                                "• Excellent performance! Keep up the good work",
                                fontSize = 13.sp,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InsightCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
