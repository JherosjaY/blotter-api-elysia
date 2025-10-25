package com.example.blottermanagementsystem.ui.screens.map

import androidx.compose.foundation.layout.*
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
fun IncidentMapScreen(
    onNavigateBack: () -> Unit,
    onReportClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Group by location
    val reportsByLocation = remember(allReports, selectedFilter) {
        allReports
            .filter { selectedFilter == "All" || it.incidentType == selectedFilter }
            .groupBy { it.incidentLocation }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Incident Map",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
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
            // Info Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = InfoBlue.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = InfoBlue
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Location-Based View",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "View incidents grouped by location",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == "Theft",
                    onClick = { selectedFilter = "Theft" },
                    label = { Text("Theft") }
                )
                FilterChip(
                    selected = selectedFilter == "Assault",
                    onClick = { selectedFilter = "Assault" },
                    label = { Text("Assault") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                reportsByLocation.forEach { (location, reports) ->
                    LocationCard(
                        location = location,
                        incidentCount = reports.size,
                        reports = reports,
                        onReportClick = onReportClick
                    )
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    location: String,
    incidentCount: Int,
    reports: List<com.example.blottermanagementsystem.data.entity.BlotterReport>,
    onReportClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            location,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$incidentCount incident${if (incidentCount > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                reports.forEach { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onReportClick(report.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    report.caseNumber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                                Text(
                                    report.incidentType,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = when (report.status) {
                                    "Resolved" -> SuccessGreen.copy(alpha = 0.2f)
                                    "Pending" -> WarningOrange.copy(alpha = 0.2f)
                                    else -> InfoBlue.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    report.status,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = when (report.status) {
                                        "Resolved" -> SuccessGreen
                                        "Pending" -> WarningOrange
                                        else -> InfoBlue
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
