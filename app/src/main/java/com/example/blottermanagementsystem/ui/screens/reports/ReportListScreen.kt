package com.example.blottermanagementsystem.ui.screens.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int, Boolean) -> Unit,  // Added isEditable parameter
    onNavigateToAdd: () -> Unit,
    initialFilter: String = "All",
    autoFocusSearch: Boolean = false,
    viewModel: DashboardViewModel = viewModel()
) {
    val reports by viewModel.allReports.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var showAdvancedFilter by remember { mutableStateOf(false) }
    
    // Advanced filter states
    var sortBy by remember { mutableStateOf("Newest First") }
    var selectedIncidentTypes by remember { mutableStateOf(setOf<String>()) }
    var showArchived by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    
    // Auto-focus search field if requested
    LaunchedEffect(autoFocusSearch) {
        if (autoFocusSearch) {
            focusRequester.requestFocus()
        }
    }
    
    // Get all unique incident types
    val allIncidentTypes = remember(reports) {
        reports.map { it.incidentType }.distinct().sorted()
    }
    
    val filteredReports = reports
        .filter { report ->
            val matchesSearch = report.caseNumber.contains(searchQuery, ignoreCase = true) ||
                    report.complainantName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Pending" -> report.status == "Pending" || report.status == "Assigned"
                else -> report.status == selectedFilter
            }
            val matchesIncidentType = selectedIncidentTypes.isEmpty() || 
                    selectedIncidentTypes.contains(report.incidentType)
            val matchesArchived = showArchived || !report.isArchived
            val matchesDateRange = (startDate == null || report.dateFiled >= startDate!!) &&
                    (endDate == null || report.dateFiled <= endDate!!)
            
            matchesSearch && matchesFilter && matchesIncidentType && matchesArchived && matchesDateRange
        }
        .let { list ->
            when (sortBy) {
                "Newest First" -> list.sortedByDescending { it.dateFiled }
                "Oldest First" -> list.sortedBy { it.dateFiled }
                "Case Number" -> list.sortedBy { it.caseNumber }
                "Status" -> list.sortedBy { it.status }
                else -> list
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdvancedFilter = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Advanced Filters",
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Search by case number or name...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = ElectricBlue)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Filter Chips (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter options with shorter text
                val filters = listOf(
                    "All" to null,
                    "Pending" to null,
                    "Investigating" to null,
                    "Resolved" to Icons.Default.Check
                )
                
                filters.forEach { (filter, icon) ->
                    FilterChip(
                        selected = selectedFilter == filter || 
                                  (selectedFilter == "Under Investigation" && filter == "Investigating"),
                        onClick = { 
                            selectedFilter = if (filter == "Investigating") "Under Investigation" else filter
                        },
                        label = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(filter, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = TextPrimary
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
                        Icon(
                            Icons.Default.List,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No reports found",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = true
                ) {
                    items(filteredReports) { report ->
                        ReportCard(
                            report = report,
                            onClick = { 
                                // Only editable if in Pending tab AND status is Pending or Assigned
                                val isEditable = selectedFilter == "Pending" && 
                                                (report.status == "Pending" || report.status == "Assigned")
                                onNavigateToDetail(report.id, isEditable)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Advanced Filter Dialog
    if (showAdvancedFilter) {
        AdvancedFilterDialog(
            sortBy = sortBy,
            onSortByChange = { sortBy = it },
            selectedIncidentTypes = selectedIncidentTypes,
            onIncidentTypesChange = { selectedIncidentTypes = it },
            allIncidentTypes = allIncidentTypes,
            showArchived = showArchived,
            onShowArchivedChange = { showArchived = it },
            onDismiss = { showAdvancedFilter = false },
            onReset = {
                sortBy = "Newest First"
                selectedIncidentTypes = setOf()
                showArchived = false
                startDate = null
                endDate = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterDialog(
    sortBy: String,
    onSortByChange: (String) -> Unit,
    selectedIncidentTypes: Set<String>,
    onIncidentTypesChange: (Set<String>) -> Unit,
    allIncidentTypes: List<String>,
    showArchived: Boolean,
    onShowArchivedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Filters",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sort By Section
                item {
                    Column {
                        Text(
                            text = "Sort By",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val sortOptions = listOf("Newest First", "Oldest First", "Case Number", "Status")
                        sortOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSortByChange(option) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sortBy == option,
                                    onClick = { onSortByChange(option) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ElectricBlue
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                // Incident Types Section
                if (allIncidentTypes.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Incident Types",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            allIncidentTypes.forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onIncidentTypesChange(
                                                if (selectedIncidentTypes.contains(type)) {
                                                    selectedIncidentTypes - type
                                                } else {
                                                    selectedIncidentTypes + type
                                                }
                                            )
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedIncidentTypes.contains(type),
                                        onCheckedChange = {
                                            onIncidentTypesChange(
                                                if (it) selectedIncidentTypes + type
                                                else selectedIncidentTypes - type
                                            )
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = ElectricBlue
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = type,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // View Options Section
                item {
                    Column {
                        Text(
                            text = "View Options",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShowArchivedChange(!showArchived) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Show Archived Reports",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                            Switch(
                                checked = showArchived,
                                onCheckedChange = onShowArchivedChange,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ElectricBlue,
                                    checkedTrackColor = ElectricBlue.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onReset,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ElectricBlue
                )
            ) {
                Text("Reset")
            }
        }
    )
}

@Composable
fun ReportCard(
    report: BlotterReport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.caseNumber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Text(
                        text = report.complainantName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (report.status) {
                        "Pending" -> WarningOrange.copy(alpha = 0.2f)
                        "Assigned" -> WarningYellow.copy(alpha = 0.2f)
                        "Under Investigation" -> InfoBlue.copy(alpha = 0.2f)
                        "Resolved" -> SuccessGreen.copy(alpha = 0.2f)
                        else -> ArchivedGray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = report.status,
                        fontSize = 12.sp,
                        color = when (report.status) {
                            "Pending" -> WarningOrange
                            "Assigned" -> WarningYellow
                            "Under Investigation" -> InfoBlue
                            "Resolved" -> SuccessGreen
                            else -> ArchivedGray
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.incidentLocation,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = report.incidentType,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Evidence count indicator
            val imageCount = report.imageUris.split(",").filter { it.isNotBlank() }.size
            val videoCount = report.videoUris.split(",").filter { it.isNotBlank() }.size
            
            if (imageCount > 0 || videoCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (imageCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ElectricBlue
                            )
                            Text(
                                text = "$imageCount",
                                fontSize = 11.sp,
                                color = ElectricBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    if (videoCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = ElectricBlue
                            )
                            Text(
                                text = "$videoCount",
                                fontSize = 11.sp,
                                color = ElectricBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
