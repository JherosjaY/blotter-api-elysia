package com.example.blottermanagementsystem.ui.screens.admin

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
fun AdminReportDetailScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToRespondentList: (Int) -> Unit,
    onNavigateToSuspectList: (Int) -> Unit,
    onNavigateToWitnessList: (Int) -> Unit,
    onNavigateToEvidenceList: (Int) -> Unit,
    onNavigateToHearingList: (Int) -> Unit,
    onNavigateToLegalDocuments: (Int) -> Unit,
    onNavigateToCaseTimeline: (Int, String) -> Unit,
    onNavigateToEnhancedQR: (Int, String) -> Unit,
    onNavigateToAdvancedAnalytics: () -> Unit,
    onNavigateToIncidentMap: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val report = allReports.find { it.id == reportId }
    
    // Get all officers
    val allUsers by viewModel.allUsers.collectAsState(initial = emptyList())
    val allOfficers = allUsers.filter { it.role == "Officer" }
    
    var showAssignOfficerDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Case Details (Admin View)",
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
        if (report == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Report not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Admin Warning Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = WarningOrange.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                tint = WarningOrange
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Admin View Only - Officers handle investigations",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Case Info Card
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
                            Text(
                                report.caseNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Complainant: ${report.complainantName}",
                                fontSize = 14.sp
                            )
                            Text(
                                "Type: ${report.incidentType}",
                                fontSize = 14.sp
                            )
                            Text(
                                "Status: ${report.status}",
                                fontSize = 14.sp,
                                color = when (report.status) {
                                    "Resolved" -> SuccessGreen
                                    "Pending" -> WarningOrange
                                    else -> InfoBlue
                                }
                            )
                        }
                    }
                }
                
                // Timeline Button
                item {
                    OutlinedButton(
                        onClick = { onNavigateToCaseTimeline(reportId, report.caseNumber) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Case Timeline")
                    }
                }
                
                // Assign Officers Button
                item {
                    Button(
                        onClick = { showAssignOfficerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                    ) {
                        Icon(Icons.Default.AssignmentInd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assign/Reassign Officers")
                    }
                }
                
                // View Only Sections
                item {
                    Text(
                        "Case Overview (View Only)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // View Buttons - Single Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToRespondentList(reportId) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "View\nRespondents",
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToSuspectList(reportId) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "View\nSuspects",
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToWitnessList(reportId) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "View\nWitnesses",
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToEvidenceList(reportId) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "View\nEvidence",
                                    fontSize = 9.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = { onNavigateToHearingList(reportId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Hearings")
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = { onNavigateToLegalDocuments(reportId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Legal Documents & KP Forms")
                    }
                }
                
                // Admin Tools
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Admin Tools",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToEnhancedQR(reportId, report.caseNumber) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Code", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onNavigateToAdvancedAnalytics,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analytics", fontSize = 12.sp)
                        }
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToIncidentMap() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Map View", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (showAssignOfficerDialog && report != null) {
            AssignOfficerDialog(
                report = report,
                officers = allOfficers,
                onDismiss = { showAssignOfficerDialog = false },
                onAssign = { selectedOfficerIds ->
                    viewModel.assignOfficersToCase(reportId, selectedOfficerIds)
                    showAssignOfficerDialog = false
                }
            )
        }
    }
}

@Composable
fun AssignOfficerDialog(
    report: com.example.blottermanagementsystem.data.entity.BlotterReport,
    officers: List<com.example.blottermanagementsystem.data.entity.User>,
    onDismiss: () -> Unit,
    onAssign: (List<Int>) -> Unit
) {
    val currentOfficerIds = report.assignedOfficerIds.split(",")
        .filter { it.isNotBlank() }
        .mapNotNull { it.toIntOrNull() }
    
    var selectedOfficers by remember { mutableStateOf(currentOfficerIds) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Assign Officers to Case",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Case: ${report.caseNumber}",
                        fontSize = 14.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Select up to 2 officers:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(officers.size) { index ->
                    val officer = officers[index]
                    val isSelected = selectedOfficers.contains(officer.id)
                    val canSelect = selectedOfficers.size < 2 || isSelected
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ElectricBlue.copy(alpha = 0.1f) else CardBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked && canSelect) {
                                        selectedOfficers = selectedOfficers + officer.id
                                    } else if (!checked) {
                                        selectedOfficers = selectedOfficers - officer.id
                                    }
                                },
                                enabled = canSelect
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${officer.firstName} ${officer.lastName}",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Officer ID: ${officer.id}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                if (selectedOfficers.size >= 2) {
                    item {
                        Text(
                            "Maximum 2 officers selected",
                            fontSize = 12.sp,
                            color = WarningOrange,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedOfficers) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Assign Officers")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
