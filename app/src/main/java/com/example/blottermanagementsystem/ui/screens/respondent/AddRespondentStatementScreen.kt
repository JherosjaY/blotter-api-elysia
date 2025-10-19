package com.example.blottermanagementsystem.ui.screens.respondent

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
import com.example.blottermanagementsystem.data.entity.RespondentStatement
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.RespondentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRespondentStatementScreen(
    respondentId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: RespondentViewModel = viewModel()
) {
    var statement by remember { mutableStateOf("") }
    var interviewDate by remember { mutableStateOf("") }
    var interviewedBy by remember { mutableStateOf("") }
    var isUnderOath by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respondent Statement", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.RecordVoiceOver, null, tint = InfoBlue)
                    Text(
                        "Record the respondent's statement or testimony",
                        fontSize = 13.sp,
                        color = InfoBlue
                    )
                }
            }
            
            // Statement
            OutlinedTextField(
                value = statement,
                onValueChange = { statement = it },
                label = { Text("Statement *") },
                placeholder = { Text("Enter the respondent's statement verbatim") },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Interview Date
            OutlinedTextField(
                value = interviewDate,
                onValueChange = { interviewDate = it },
                label = { Text("Interview Date *") },
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
            
            // Interviewed By
            OutlinedTextField(
                value = interviewedBy,
                onValueChange = { interviewedBy = it },
                label = { Text("Interviewed By *") },
                placeholder = { Text("Name of interviewer") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Under Oath Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isUnderOath,
                    onCheckedChange = { isUnderOath = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ElectricBlue,
                        uncheckedColor = DividerColor
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Statement given under oath",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }
            
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = ErrorRed, fontSize = 14.sp)
            }
            
            // Save Button
            Button(
                onClick = {
                    when {
                        statement.isBlank() -> errorMessage = "Statement is required"
                        interviewDate.isBlank() -> errorMessage = "Interview date is required"
                        interviewedBy.isBlank() -> errorMessage = "Interviewer name is required"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    // Get blotterReportId from respondent
                                    val respondent = viewModel.getRespondentById(respondentId)
                                    if (respondent != null) {
                                        val respondentStatement = RespondentStatement(
                                            respondentId = respondentId,
                                            blotterReportId = respondent.blotterReportId,
                                            statement = statement,
                                            submittedDate = System.currentTimeMillis(),
                                            submittedVia = "In Person",
                                            isVerified = false
                                        )
                                        viewModel.addRespondentStatement(respondentStatement)
                                        onSaveSuccess()
                                    } else {
                                        errorMessage = "Respondent not found"
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to save"
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Statement")
                }
            }
        }
    }
}
