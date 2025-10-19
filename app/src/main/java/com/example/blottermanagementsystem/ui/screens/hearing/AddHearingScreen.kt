package com.example.blottermanagementsystem.ui.screens.hearing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Hearing
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHearingScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    caseNumber: String = "",
    viewModel: DashboardViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { com.example.blottermanagementsystem.utils.PreferencesManager(context) }
    val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
    
    var hearingDate by remember { mutableStateOf("") }
    var hearingTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Schedule Hearing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = WarningOrange.copy(alpha = 0.15f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Gavel,
                        null,
                        tint = WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Hearing Schedule",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                        Text(
                            "Schedule a hearing for case mediation or settlement",
                            fontSize = 12.sp,
                            color = WarningOrange.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Date
            OutlinedTextField(
                value = hearingDate,
                onValueChange = { hearingDate = it },
                label = { Text("Hearing Date *") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Time
            OutlinedTextField(
                value = hearingTime,
                onValueChange = { hearingTime = it },
                label = { Text("Hearing Time *") },
                placeholder = { Text("HH:MM (e.g., 14:00)") },
                leadingIcon = { Icon(Icons.Default.Schedule, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location *") },
                placeholder = { Text("Barangay Hall, Room 1") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Purpose
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Purpose *") },
                placeholder = { Text("Mediation, Settlement, Testimony, etc.") },
                leadingIcon = { Icon(Icons.Default.Gavel, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                placeholder = { Text("Who should attend, documents needed, etc.") },
                leadingIcon = { Icon(Icons.Default.Notes, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = ErrorRed, fontSize = 14.sp)
            }
            
            // Save Button
            Button(
                onClick = {
                    when {
                        hearingDate.isBlank() -> errorMessage = "Date is required"
                        hearingTime.isBlank() -> errorMessage = "Time is required"
                        location.isBlank() -> errorMessage = "Location is required"
                        purpose.isBlank() -> errorMessage = "Purpose is required"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    // Parse date and time
                                    val dateTimeString = "$hearingDate $hearingTime"
                                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    val scheduledDate = format.parse(dateTimeString)?.time ?: System.currentTimeMillis()
                                    
                                    val hearing = Hearing(
                                        blotterReportId = reportId,
                                        hearingDate = hearingDate,
                                        hearingTime = hearingTime,
                                        location = location,
                                        purpose = purpose,
                                        status = "Scheduled"
                                    )
                                    viewModel.addHearing(hearing, currentUserName, caseNumber)
                                    onSaveSuccess()
                                } catch (e: Exception) {
                                    errorMessage = "Invalid date/time format or ${e.message}"
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                } else {
                    Icon(Icons.Default.Event, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Schedule Hearing")
                }
            }
        }
    }
}
