package com.example.blottermanagementsystem.ui.screens.legal

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.blottermanagementsystem.utils.DateUtils
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.MediationSession
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.LegalDocumentsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediationSessionScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    viewModel: LegalDocumentsViewModel = viewModel()
) {
    val sessions by viewModel.getMediationSessionsByReportId(reportId).collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mediation Sessions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "New Session", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = SuccessGreen
            ) {
                Icon(Icons.Default.Add, "New Session")
            }
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No mediation sessions yet", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Schedule First Session")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions) { session ->
                    MediationSessionCard(session)
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateMediationDialog(
            reportId = reportId,
            onDismiss = { showCreateDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun MediationSessionCard(session: MediationSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(
                        "Session #${session.sessionNumber}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Text(
                        session.sessionType,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                
                Surface(
                    color = when (session.outcome) {
                        "Settled" -> SuccessGreen.copy(alpha = 0.2f)
                        "Failed" -> DangerRed.copy(alpha = 0.2f)
                        "Ongoing" -> WarningYellow.copy(alpha = 0.2f)
                        "Adjourned" -> InfoBlue.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        session.outcome,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = when (session.outcome) {
                            "Settled" -> SuccessGreen
                            "Failed" -> DangerRed
                            "Ongoing" -> WarningYellow
                            "Adjourned" -> InfoBlue
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${DateUtils.formatDate(session.sessionDate)} at ${session.sessionTime}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Mediator: ${session.mediatorName}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    session.venue,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            if (session.complainantPresent || session.respondentPresent) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AttendanceChip(
                        label = "Complainant",
                        present = session.complainantPresent
                    )
                    AttendanceChip(
                        label = "Respondent",
                        present = session.respondentPresent
                    )
                }
            }
            
            if (session.nextSessionScheduled) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Next: ${session.nextSessionDate?.let { DateUtils.formatDate(it) }} at ${session.nextSessionTime}",
                            fontSize = 12.sp,
                            color = InfoBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceChip(label: String, present: Boolean) {
    Surface(
        color = if (present) SuccessGreen.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (present) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (present) SuccessGreen else DangerRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                color = if (present) SuccessGreen else DangerRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMediationDialog(
    reportId: Int,
    onDismiss: () -> Unit,
    viewModel: LegalDocumentsViewModel
) {
    var mediatorName by remember { mutableStateOf("") }
    var sessionType by remember { mutableStateOf("Conciliation") }
    var recordedBy by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Mediation", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = mediatorName,
                    onValueChange = { mediatorName = it },
                    label = { Text("Mediator Name") },
                    placeholder = { Text("Lupon Chairman") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = sessionType,
                    onValueChange = { sessionType = it },
                    label = { Text("Session Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = recordedBy,
                    onValueChange = { recordedBy = it },
                    label = { Text("Recorded By") },
                    placeholder = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        viewModel.createMediationSession(
                            blotterReportId = reportId,
                            sessionDate = System.currentTimeMillis(),
                            sessionTime = "10:00 AM",
                            mediatorName = mediatorName,
                            sessionType = sessionType,
                            recordedBy = recordedBy
                        )
                        isLoading = false
                        onDismiss()
                    }
                },
                enabled = !isLoading && mediatorName.isNotBlank() && recordedBy.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Schedule")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

