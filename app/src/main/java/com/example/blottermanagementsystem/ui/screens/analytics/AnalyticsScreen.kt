package com.example.blottermanagementsystem.ui.screens.analytics

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val reports by viewModel.allReports.collectAsState(initial = emptyList())
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics & Reports",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = true
        ) {
            // Overview Stats
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
                        title = "Total",
                        value = dashboardStats.totalReports.toString(),
                        icon = Icons.Default.Assessment,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending",
                        value = dashboardStats.pendingReports.toString(),
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
                    StatCard(
                        title = "Ongoing",
                        value = dashboardStats.ongoingReports.toString(),
                        icon = Icons.Default.Autorenew,
                        color = InfoBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Resolved",
                        value = dashboardStats.resolvedReports.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Status Distribution Pie Chart
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status Distribution",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )
                ) {
                    StatusPieChart(
                        pending = dashboardStats.pendingReports,
                        ongoing = dashboardStats.ongoingReports,
                        resolved = dashboardStats.resolvedReports
                    )
                }
            }
            
            // Monthly Trend Bar Chart
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monthly Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )
                ) {
                    MonthlyBarChart(reports = reports)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
fun StatusPieChart(
    pending: Int,
    ongoing: Int,
    resolved: Int
) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setDrawEntryLabels(false)
                legend.textColor = android.graphics.Color.WHITE
                legend.textSize = 12f
                setHoleColor(android.graphics.Color.TRANSPARENT)
                holeRadius = 58f
                transparentCircleRadius = 61f
            }
        },
        update = { chart ->
            val entries = mutableListOf<PieEntry>()
            if (pending > 0) entries.add(PieEntry(pending.toFloat(), "Pending"))
            if (ongoing > 0) entries.add(PieEntry(ongoing.toFloat(), "Ongoing"))
            if (resolved > 0) entries.add(PieEntry(resolved.toFloat(), "Resolved"))
            
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(
                    WarningOrange.toArgb(),
                    InfoBlue.toArgb(),
                    SuccessGreen.toArgb()
                )
                valueTextSize = 14f
                valueTextColor = android.graphics.Color.WHITE
            }
            
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun MonthlyBarChart(reports: List<com.example.blottermanagementsystem.data.entity.BlotterReport>) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textColor = android.graphics.Color.WHITE
                xAxis.setDrawGridLines(false)
                axisLeft.textColor = android.graphics.Color.WHITE
                axisLeft.setDrawGridLines(false)
                axisRight.isEnabled = false
                setFitBars(true)
            }
        },
        update = { chart ->
            val monthCounts = reports.groupBy { report ->
                java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault())
                    .format(java.util.Date(report.dateFiled))
            }.mapValues { it.value.size }
            
            val entries = monthCounts.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }
            
            val dataSet = BarDataSet(entries, "Reports").apply {
                color = ElectricBlue.toArgb()
                valueTextSize = 12f
                valueTextColor = android.graphics.Color.WHITE
            }
            
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(monthCounts.keys.toList())
            chart.data = BarData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}
