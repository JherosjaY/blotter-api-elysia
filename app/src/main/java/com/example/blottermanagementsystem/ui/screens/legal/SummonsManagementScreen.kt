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
import com.example.blottermanagementsystem.data.entity.Summons
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.LegalDocumentsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummonsManagementScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    viewModel: LegalDocumentsViewModel = viewModel()
) {
    val summonsList by viewModel.getSummonsByReportId(reportId).collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summons Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "Create Summons", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = ElectricBlue
            ) {
                Icon(Icons.Default.Add, "Create Summons")
            }
        }
    ) { padding ->
        if (summonsList.isEmpty()) {
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
                        Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No summons issued yet", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Issue First Summons")
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
                items(summonsList) { summons ->
                    SummonsCard(summons)
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateSummonsDialog(
            reportId = reportId,
            onDismiss = { showCreateDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun SummonsCard(summons: Summons) {
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
                Text(
                    summons.summonsNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                
                Surface(
                    color = when (summons.deliveryStatus) {
                        "Served" -> SuccessGreen.copy(alpha = 0.2f)
                        "Pending" -> WarningYellow.copy(alpha = 0.2f)
                        "Refused" -> DangerRed.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        summons.deliveryStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = when (summons.deliveryStatus) {
                            "Served" -> SuccessGreen
                            "Pending" -> WarningYellow
                            "Refused" -> DangerRed
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                summons.summonsType,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                "Purpose: ${summons.purpose}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            if (summons.hearingDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = InfoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        DateUtils.formatDate(summons.hearingDate) + " at ${summons.hearingTime}",
                        fontSize = 12.sp,
                        color = InfoBlue
                    )
                }
            }
            
            if (summons.isComplied) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Complied",
                        fontSize = 12.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSummonsDialog(
    reportId: Int,
    onDismiss: () -> Unit,
    viewModel: LegalDocumentsViewModel
) {
    var summonsType by remember { mutableStateOf("First Notice") }
    var purpose by remember { mutableStateOf("Mediation") }
    var issuedBy by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedRespondentId by remember { mutableStateOf<Int?>(null) }
    var showRespondentMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Get respondents for this report
    val respondents by viewModel.getRespondentsByReportId(reportId).collectAsState(initial = emptyList())
    val selectedRespondent = respondents.find { it.id == selectedRespondentId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Summons", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = summonsType,
                    onValueChange = { summonsType = it },
                    label = { Text("Summons Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Purpose") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Respondent Selector
                ExposedDropdownMenuBox(
                    expanded = showRespondentMenu,
                    onExpandedChange = { showRespondentMenu = it }
                ) {
                    OutlinedTextField(
                        value = if (selectedRespondent != null) "Respondent ID: ${selectedRespondent.personId}" else "Select Respondent",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Respondent *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRespondentMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showRespondentMenu,
                        onDismissRequest = { showRespondentMenu = false }
                    ) {
                        if (respondents.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No respondents added yet", color = Color.Gray) },
                                onClick = { }
                            )
                        } else {
                            respondents.forEach { respondent ->
                                DropdownMenuItem(
                                    text = { Text("Respondent - ${respondent.contactNumber}") },
                                    onClick = {
                                        selectedRespondentId = respondent.id
                                        showRespondentMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = issuedBy,
                    onValueChange = { issuedBy = it },
                    label = { Text("Issued By") },
                    placeholder = { Text("Punong Barangay Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRespondentId != null) {
                        isLoading = true
                        scope.launch {
                            viewModel.createSummons(
                                blotterReportId = reportId,
                                respondentId = selectedRespondentId!!,
                                summonsType = summonsType,
                                hearingDate = null,
                                hearingTime = null,
                                purpose = purpose,
                                issuedBy = issuedBy
                            )
                            isLoading = false
                            onDismiss()
                        }
                    }
                },
                enabled = !isLoading && issuedBy.isNotBlank() && selectedRespondentId != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Issue Summons")
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

