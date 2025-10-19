package com.example.blottermanagementsystem.ui.screens.respondent

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Respondent
import com.example.blottermanagementsystem.data.entity.RespondentStatement
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.RespondentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RespondentDetailScreen(
    respondentId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddStatement: (Int) -> Unit,
    viewModel: RespondentViewModel = viewModel()
) {
    var respondent by remember { mutableStateOf<Respondent?>(null) }
    val statements by viewModel.getRespondentStatements(respondentId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var showMarkAppearedDialog by remember { mutableStateOf(false) }
    var showElevateDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showSendNotificationDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(respondentId) {
        respondent = viewModel.getRespondentById(respondentId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respondent Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        respondent?.let { resp ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Basic Information",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            InfoRow("Accusation", resp.accusation)
                            InfoRow("Contact", resp.contactNumber)
                            resp.relationshipToComplainant?.let {
                                InfoRow("Relationship", it)
                            }
                            InfoRow("Status", resp.status)
                            InfoRow("Cooperation", resp.cooperationStatus)
                            
                            if (resp.hasEvidence) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = WarningYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Evidence Found",
                                        color = WarningYellow,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Actions Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Actions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (!resp.notificationSent) {
                                Button(
                                    onClick = { showSendNotificationDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send Initial Notification")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (resp.cooperationStatus != "Appeared") {
                                Button(
                                    onClick = { showMarkAppearedDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark as Appeared")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (resp.status != "Elevated to Suspect") {
                                Button(
                                    onClick = { showElevateDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Elevate to Suspect")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (resp.status != "Cleared") {
                                OutlinedButton(
                                    onClick = { showClearDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Clear Respondent")
                                }
                            }
                        }
                    }
                }
                
                // Statements Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Statements (${statements.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        IconButton(onClick = { onNavigateToAddStatement(respondentId) }) {
                            Icon(Icons.Default.Add, "Add Statement", tint = ElectricBlue)
                        }
                    }
                }
                
                if (statements.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No statements recorded",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(statements) { statement ->
                        StatementCard(statement)
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showSendNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showSendNotificationDialog = false },
            title = { Text("Send Notification") },
            text = { Text("Send initial notification SMS to respondent?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.sendInitialNotification(respondentId)
                            showSendNotificationDialog = false
                        }
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendNotificationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showMarkAppearedDialog) {
        var statement by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMarkAppearedDialog = false },
            title = { Text("Mark as Appeared") },
            text = {
                Column {
                    Text("Record respondent's statement (optional):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = statement,
                        onValueChange = { statement = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter statement...") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.markAsAppeared(
                                respondentId,
                                statement.ifBlank { null }
                            )
                            showMarkAppearedDialog = false
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAppearedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showElevateDialog) {
        var evidence by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showElevateDialog = false },
            title = { Text("Elevate to Suspect") },
            text = {
                Column {
                    OutlinedTextField(
                        value = evidence,
                        onValueChange = { evidence = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Evidence Found") },
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Investigation Notes") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.elevateToSuspect(respondentId, evidence, notes)
                            showElevateDialog = false
                        }
                    },
                    enabled = evidence.isNotBlank()
                ) {
                    Text("Elevate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showElevateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showClearDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Respondent") },
            text = {
                Column {
                    Text("Reason for clearing:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter reason...") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.clearRespondent(respondentId, reason)
                            showClearDialog = false
                        }
                    },
                    enabled = reason.isNotBlank()
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(120.dp)
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatementCard(statement: RespondentStatement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatDate(statement.submittedDate),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                if (statement.isVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Verified",
                            fontSize = 12.sp,
                            color = SuccessGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                statement.statement,
                fontSize = 14.sp,
                color = Color.White
            )
            
            // Note: isUnderOath field removed from entity
            // Uncomment when field is added back to RespondentStatement entity
            /*
            if (statement.isUnderOath) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Gavel,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Under Oath",
                        fontSize = 12.sp,
                        color = WarningYellow
                    )
                }
            }
            */
            
            statement.officerNotes?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Officer Notes:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    notes,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
