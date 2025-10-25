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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerReportDetailScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddRespondent: (Int) -> Unit,
    onNavigateToRespondentList: (Int) -> Unit,
    onNavigateToAddSuspect: (Int) -> Unit,
    onNavigateToSuspectList: (Int) -> Unit,
    onNavigateToAddWitness: (Int) -> Unit,
    onNavigateToWitnessList: (Int) -> Unit,
    onNavigateToAddEvidence: (Int) -> Unit,
    onNavigateToEvidenceList: (Int) -> Unit,
    onNavigateToAddHearing: (Int) -> Unit,
    onNavigateToHearingList: (Int) -> Unit,
    onNavigateToAddResolution: (Int) -> Unit,
    onNavigateToLegalDocuments: (Int) -> Unit,
    onNavigateToCaseTimeline: (Int, String) -> Unit,
    onNavigateToPhotoGallery: (Int) -> Unit,
    onNavigateToEnhancedQR: (Int, String) -> Unit,
    onNavigateToHearingCalendar: () -> Unit,
    onNavigateToIncidentMap: () -> Unit,
    onNavigateToVoiceToText: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val allReports by viewModel.allReports.collectAsState(initial = emptyList())
    val report = allReports.find { it.id == reportId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Investigation Details",
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
                // Officer Badge
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ElectricBlue.copy(alpha = 0.15f)
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
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Officer Mode - Full Investigation Access",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        }
                    }
                }
                
                // Case Info
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
                            Text("Complainant: ${report.complainantName}", fontSize = 14.sp)
                            Text("Type: ${report.incidentType}", fontSize = 14.sp)
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
                
                // Timeline
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
                
                // Investigation Tools
                item {
                    Text(
                        "Investigation Tools",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Respondents
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToAddRespondent(reportId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null)
                                Text("Add\nRespondent", fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToRespondentList(reportId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Person, contentDescription = null)
                                Text("View\nRespondents", fontSize = 11.sp)
                            }
                        }
                    }
                }
                
                // Suspects
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToAddSuspect(reportId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null)
                                Text("Add\nSuspect", fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToSuspectList(reportId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null)
                                Text("View\nSuspects", fontSize = 11.sp)
                            }
                        }
                    }
                }
                
                // Witnesses
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToAddWitness(reportId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Text("Add\nWitness", fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToWitnessList(reportId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Text("View\nWitnesses", fontSize = 11.sp)
                            }
                        }
                    }
                }
                
                // Evidence
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToAddEvidence(reportId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Description, contentDescription = null)
                                Text("Add\nEvidence", fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { onNavigateToEvidenceList(reportId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Description, contentDescription = null)
                                Text("View\nEvidence", fontSize = 11.sp)
                            }
                        }
                    }
                }
                
                // Hearings & Resolution
                item {
                    Button(
                        onClick = { onNavigateToAddHearing(reportId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Schedule Hearing")
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = { onNavigateToHearingList(reportId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View All Hearings")
                    }
                }
                
                item {
                    Button(
                        onClick = { onNavigateToAddResolution(reportId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Propose Resolution")
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = { onNavigateToLegalDocuments(reportId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Legal Documents & KP Forms")
                    }
                }
                
                // Officer Tools
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Officer Tools",
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
                            onClick = { onNavigateToPhotoGallery(reportId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onNavigateToEnhancedQR(reportId, report.caseNumber) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Code", fontSize = 12.sp)
                        }
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = onNavigateToHearingCalendar,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hearing Calendar")
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
                            Text("Map", fontSize = 12.sp)
                        }
                    }
                }
                
                item {
                    OutlinedButton(
                        onClick = onNavigateToVoiceToText,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voice-to-Text Report")
                    }
                }
            }
        }
    }
}
