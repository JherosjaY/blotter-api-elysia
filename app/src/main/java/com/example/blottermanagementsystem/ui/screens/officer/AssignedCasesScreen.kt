package com.example.blottermanagementsystem.ui.screens.officer

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.PreferencesManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignedCasesScreen(
    officerId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToReportDetail: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    
    // Filter reports assigned to this officer
    val assignedCases = remember(allReports, officerId) {
        allReports.filter { report ->
            report.assignedOfficerIds.split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { it.toIntOrNull() }
                .contains(officerId)
        }
    }
    
    val pendingCases = assignedCases.filter { 
        it.status == "Pending" || it.status == "Assigned" || it.status == "Under Investigation" 
    }
    val resolvedCases = assignedCases.filter { it.status == "Resolved" }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active Cases", "Resolved")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Assigned Cases",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${assignedCases.size} total cases",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Statistics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active",
                    count = pendingCases.size,
                    color = WarningOrange,
                    icon = Icons.Default.Assignment,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Resolved",
                    count = resolvedCases.size,
                    color = SuccessGreen,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardBackground,
                contentColor = ElectricBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElectricBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            // Cases List
            val displayCases = if (selectedTab == 0) pendingCases else resolvedCases
            
            if (displayCases.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.AssignmentLate else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextTertiary
                        )
                        Text(
                            text = if (selectedTab == 0) "No active cases" else "No resolved cases",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (selectedTab == 0) "Cases assigned to you will appear here" else "Completed cases will appear here",
                            fontSize = 14.sp,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayCases) { report ->
                        CaseCard(
                            report = report,
                            onClick = { onNavigateToReportDetail(report.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = count.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        }
    }
}

@Composable
private fun CaseCard(
    report: BlotterReport,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Case #${report.caseNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (report.status) {
                        "Pending" -> WarningOrange.copy(alpha = 0.2f)
                        "Under Investigation" -> InfoBlue.copy(alpha = 0.2f)
                        "Resolved" -> SuccessGreen.copy(alpha = 0.2f)
                        else -> ArchivedGray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = report.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (report.status) {
                            "Pending" -> WarningOrange
                            "Under Investigation" -> InfoBlue
                            "Resolved" -> SuccessGreen
                            else -> ArchivedGray
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Incident Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = report.incidentType,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Complainant
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = report.complainantName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date Filed
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filed: ${dateFormat.format(Date(report.dateFiled))}",
                    fontSize = 12.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
