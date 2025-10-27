package com.example.blottermanagementsystem.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AdminViewModel
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.utils.rememberDebouncedState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportOversightScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReportDetail: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val officers by viewModel.allOfficers.collectAsState() // Now uses StateFlow from cloud
    val scope = rememberCoroutineScope()
    
    var filterStatus by remember { mutableStateOf("All") }
    var searchQuery by rememberDebouncedState("", delayMillis = 300)
    var selectedReport by remember { mutableStateOf<BlotterReport?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    
    val statusFilters = listOf("All", "Pending", "Assigned", "Under Investigation", "Resolved", "Closed")
    
    val filteredReports = remember(allReports, filterStatus, searchQuery) { allReports.filter { report ->
        val matchesStatus = filterStatus == "All" || report.status == filterStatus
        val matchesSearch = searchQuery.isEmpty() || 
                           report.caseNumber.contains(searchQuery, ignoreCase = true) ||
                           report.complainantName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    } }
    
    val paginationState = rememberPaginationState(filteredReports, pageSize = 20)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Report Oversight",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredReports.size} reports",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search reports") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters.size) { index ->
                    val status = statusFilters[index]
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Reports List
            if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📋", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No reports found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val listState = LazyListOptimizer.rememberOptimizedLazyListState()
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(LazyListOptimizer.OPTIMAL_CONTENT_PADDING),
                    verticalArrangement = Arrangement.spacedBy(LazyListOptimizer.OPTIMAL_ITEM_SPACING)
                ) {
                    items(paginationState.visibleItems, key = { it.id }) { report ->
                        AdminReportCard(
                            report = report,
                            onClick = { onNavigateToReportDetail(report.id) },
                            onAssignOfficer = {
                                selectedReport = report
                                showAssignDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Assign Officer Dialog
    if (showAssignDialog && selectedReport != null) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Officer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select an officer to assign to this case:")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(officers) { officer ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            viewModel.assignOfficersToCase(
                                                selectedReport!!.id,
                                                listOf(officer.userId ?: 0)
                                            )
                                            showAssignDialog = false
                                            selectedReport = null
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = officer.name,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = officer.rank,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${officer.assignedCases} cases",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun AdminReportCard(
    report: BlotterReport,
    onClick: () -> Unit,
    onAssignOfficer: () -> Unit
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.caseNumber,
                    fontSize = 18.sp,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (report.status) {
                            "Pending" -> WarningOrange
                            "Under Investigation" -> InfoBlue
                            "Resolved" -> SuccessGreen
                            else -> TextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Details
            Text(
                text = "Complainant: ${report.complainantName}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Type: ${report.incidentType}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Filed: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(report.dateFiled))}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Assigned Officer or Assign Button
            Spacer(modifier = Modifier.height(12.dp))
            if (report.assignedOfficerId != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Assigned to Officer",
                        fontSize = 12.sp,
                        color = InfoBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (report.status == "Pending") {
                OutlinedButton(
                    onClick = onAssignOfficer,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    )
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assign Officer")
                }
            }
        }
    }
}
