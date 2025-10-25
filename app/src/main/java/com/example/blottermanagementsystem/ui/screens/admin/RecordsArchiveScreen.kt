package com.example.blottermanagementsystem.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.utils.rememberDebouncedState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsArchiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReportDetail: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    
    var filterStatus by remember { mutableStateOf("All") }
    var searchQuery by rememberDebouncedState("", delayMillis = 300)
    
    val statusFilters = listOf("All", "Pending", "Assigned", "Under Investigation", "Resolved", "Closed", "Archived")
    
    val filteredReports = remember(allReports, filterStatus, searchQuery) {
        allReports.filter { report ->
            val matchesStatus = when (filterStatus) {
                "All" -> true
                "Archived" -> report.isArchived
                else -> report.status == filterStatus && !report.isArchived
            }
            val matchesSearch = searchQuery.isEmpty() || 
                               report.caseNumber.contains(searchQuery, ignoreCase = true) ||
                               report.complainantName.contains(searchQuery, ignoreCase = true) ||
                               report.incidentType.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }.sortedByDescending { it.dateFiled }
    }
    
    val paginationState = rememberPaginationState(filteredReports, pageSize = 20)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Records Archive",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredReports.size} records",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by case #, complainant, or type...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters.size) { index ->
                    val status = statusFilters[index]
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status) },
                        leadingIcon = if (filterStatus == status) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Records List
            if (filteredReports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No records found",
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
                    items(paginationState.visibleItems.size) { index ->
                        val report = paginationState.visibleItems[index]
                        ArchiveReportCard(
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
private fun ArchiveReportCard(
    report: BlotterReport,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
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
                
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (report.status) {
                        "Pending" -> WarningOrange.copy(alpha = 0.2f)
                        "Assigned" -> InfoBlue.copy(alpha = 0.2f)
                        "Under Investigation" -> InfoBlue.copy(alpha = 0.2f)
                        "Resolved" -> SuccessGreen.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = if (report.isArchived) "Archived" else report.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (report.status) {
                            "Pending" -> WarningOrange
                            "Assigned" -> InfoBlue
                            "Under Investigation" -> InfoBlue
                            "Resolved" -> SuccessGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Complainant
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.complainantName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Incident Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.incidentType,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date Filed
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Filed: ${dateFormat.format(Date(report.dateFiled))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Assigned Officer (if any)
            if (report.assignedOfficerId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Officer ID: ${report.assignedOfficerId}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
