package com.example.blottermanagementsystem.ui.screens.officer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerAnalyticsScreen(
    officerId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    
    // Filter reports assigned to this officer
    val myReports = allReports.filter { report ->
        report.assignedOfficerIds.split(",").map { it.trim().toIntOrNull() }.contains(officerId)
    }
    
    val totalCases = myReports.size
    val pendingCases = myReports.count { it.status == "Pending" || it.status == "Assigned" }
    val ongoingCases = myReports.count { 
        it.status == "Under Investigation" || it.status == "For Mediation" || it.status == "Mediation Ongoing"
    }
    val resolvedCases = myReports.count { it.status == "Resolved" || it.status == "Settled" }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Analytics",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview Stats
            item {
                Text(
                    text = "My Cases Overview",
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
                    OfficerStatCard(
                        title = "Total",
                        value = totalCases.toString(),
                        icon = Icons.Default.Assignment,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    OfficerStatCard(
                        title = "Pending",
                        value = pendingCases.toString(),
                        icon = Icons.Default.Schedule,
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
                    OfficerStatCard(
                        title = "Ongoing",
                        value = ongoingCases.toString(),
                        icon = Icons.Default.Autorenew,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                    OfficerStatCard(
                        title = "Resolved",
                        value = resolvedCases.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Performance Summary
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Performance Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PerformanceRow(
                            label = "Cases Assigned",
                            value = totalCases.toString(),
                            icon = Icons.Default.Assignment,
                            color = ElectricBlue
                        )
                        Divider(color = DividerColor)
                        PerformanceRow(
                            label = "Cases Resolved",
                            value = resolvedCases.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = SuccessGreen
                        )
                        Divider(color = DividerColor)
                        PerformanceRow(
                            label = "Resolution Rate",
                            value = if (totalCases > 0) "${(resolvedCases * 100 / totalCases)}%" else "0%",
                            icon = Icons.Default.TrendingUp,
                            color = if (totalCases > 0 && resolvedCases * 100 / totalCases >= 50) SuccessGreen else WarningOrange
                        )
                    }
                }
            }
            
            // Case Status Breakdown
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Case Status Breakdown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusBreakdownRow(
                            status = "Pending",
                            count = pendingCases,
                            total = totalCases,
                            color = WarningOrange
                        )
                        StatusBreakdownRow(
                            status = "Under Investigation",
                            count = myReports.count { it.status == "Under Investigation" },
                            total = totalCases,
                            color = InfoBlue
                        )
                        StatusBreakdownRow(
                            status = "For Mediation",
                            count = myReports.count { it.status == "For Mediation" || it.status == "Mediation Ongoing" },
                            total = totalCases,
                            color = Color(0xFF9C27B0)
                        )
                        StatusBreakdownRow(
                            status = "Resolved",
                            count = resolvedCases,
                            total = totalCases,
                            color = SuccessGreen
                        )
                    }
                }
            }
            
            // Empty state
            if (totalCases == 0) {
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
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No cases assigned yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Analytics will appear when you have assigned cases",
                                fontSize = 13.sp,
                                color = TextTertiary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun OfficerStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PerformanceRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun StatusBreakdownRow(
    status: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = status,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count ${if (count == 1) "case" else "cases"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = if (total > 0) count.toFloat() / total.toFloat() else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
