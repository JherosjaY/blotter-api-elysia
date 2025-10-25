package com.example.blottermanagementsystem.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    onNavigateBack: () -> Unit,
    onReportClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedType by remember { mutableStateOf("All") }
    
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    
    val filteredReports = remember(searchQuery, selectedStatus, selectedType, allReports) {
        allReports.filter { report ->
            val matchesQuery = searchQuery.isEmpty() || 
                report.caseNumber.contains(searchQuery, ignoreCase = true) ||
                report.complainantName.contains(searchQuery, ignoreCase = true) ||
                report.narrative.contains(searchQuery, ignoreCase = true)
            
            val matchesStatus = selectedStatus == "All" || report.status == selectedStatus
            val matchesType = selectedType == "All" || report.incidentType == selectedType
            
            matchesQuery && matchesStatus && matchesType
        }
    }
    
    val statuses = listOf("All", "Pending", "Under Investigation", "Resolved", "Archived")
    val types = listOf("All", "Theft", "Assault", "Noise Complaint", "Boundary Dispute", "Domestic Issue")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Advanced Search",
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
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by case #, name, description...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filters
            Text(
                "Filters",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status filter
            Text(
                "Status",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.take(3).forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status, fontSize = 12.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Type filter
            Text(
                "Incident Type",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.take(3).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type, fontSize = 12.sp) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Results
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Results (${filteredReports.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (searchQuery.isNotEmpty() || selectedStatus != "All" || selectedType != "All") {
                    TextButton(
                        onClick = {
                            searchQuery = ""
                            selectedStatus = "All"
                            selectedType = "All"
                        }
                    ) {
                        Text("Clear All", color = ElectricBlue, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Results list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredReports.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No results found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredReports) { report ->
                        SearchResultCard(
                            report = report,
                            onClick = { onReportClick(report.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    report: BlotterReport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    report.caseNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                Surface(
                    color = when (report.status) {
                        "Resolved" -> SuccessGreen.copy(alpha = 0.2f)
                        "Pending" -> WarningOrange.copy(alpha = 0.2f)
                        else -> InfoBlue.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        report.status,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = when (report.status) {
                            "Resolved" -> SuccessGreen
                            "Pending" -> WarningOrange
                            else -> InfoBlue
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                report.complainantName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                report.incidentType,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
