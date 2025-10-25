package com.example.blottermanagementsystem.ui.screens.respondent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.RespondentViewModel
import com.example.blottermanagementsystem.viewmodel.PersonViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRespondentScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    respondentViewModel: RespondentViewModel = viewModel(),
    personViewModel: PersonViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var accusation by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var hasEvidence by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Phone validation function
    fun validatePhoneNumber(phone: String): Boolean {
        val pattern09 = "^09\\d{9}$".toRegex()
        return pattern09.matches(phone)
    }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Respondent", fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add a person named as respondent in this case",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
            
            // Personal Information Section
            Text(
                "Personal Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                    cursorColor = ElectricBlue
                ),
                singleLine = true
            )
            
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                    cursorColor = ElectricBlue
                ),
                singleLine = true
            )
            
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { 
                    contactNumber = it
                    contactError = if (it.isNotBlank() && !validatePhoneNumber(it)) {
                        when {
                            !it.startsWith("09") -> "Must start with 09"
                            it.length < 11 -> "Need ${11 - it.length} more digit(s)"
                            it.length > 11 -> "Too long (max 11 digits)"
                            else -> "Invalid format"
                        }
                    } else null
                },
                label = { Text("Contact Number") },
                placeholder = { Text("09XXXXXXXXX") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = contactError != null,
                supportingText = {
                    if (contactError != null) {
                        Text(
                            text = contactError!!,
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Format: 09XXXXXXXXX (11 digits)",
                            fontSize = 12.sp
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                    cursorColor = ElectricBlue,
                    errorBorderColor = ErrorRed
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                }
            )
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                    cursorColor = ElectricBlue
                ),
                minLines = 2
            )
            
            Divider(color = Color.Gray.copy(alpha = 0.3f))
            
            // Case Information Section
            Text(
                "Case Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            OutlinedTextField(
                value = accusation,
                onValueChange = { accusation = it },
                label = { Text("Accusation/Charge") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                    cursorColor = ElectricBlue
                ),
                minLines = 3,
                placeholder = { Text("Describe the accusation...") }
            )
            
            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship to Complainant (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                    cursorColor = ElectricBlue
                ),
                singleLine = true,
                placeholder = { Text("e.g., Neighbor, Relative, Stranger") }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasEvidence,
                    onCheckedChange = { hasEvidence = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ElectricBlue,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Evidence Available",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Check if there's evidence against this respondent",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Error Message
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = DangerRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            errorMessage,
                            fontSize = 14.sp,
                            color = DangerRed
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Save Button
            Button(
                onClick = {
                    when {
                        firstName.isBlank() -> errorMessage = "First name is required"
                        lastName.isBlank() -> errorMessage = "Last name is required"
                        contactNumber.isBlank() -> errorMessage = "Contact number is required"
                        accusation.isBlank() -> errorMessage = "Accusation is required"
                        else -> {
                            isLoading = true
                            errorMessage = ""
                            scope.launch {
                                try {
                                    // Find or create person
                                    val personId = personViewModel.findOrCreatePerson(
                                        firstName = firstName,
                                        lastName = lastName,
                                        contactNumber = contactNumber,
                                        address = address.ifBlank { null },
                                        personType = "Respondent"
                                    )
                                    
                                    // Create respondent
                                    respondentViewModel.createRespondent(
                                        blotterReportId = reportId,
                                        personId = personId.toInt(),
                                        accusation = accusation,
                                        relationshipToComplainant = relationship.ifBlank { null },
                                        contactNumber = contactNumber,
                                        hasEvidence = hasEvidence
                                    )
                                    
                                    isLoading = false
                                    onSaveSuccess()
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Failed to add respondent: ${e.message}"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Respondent", fontSize = 16.sp)
                }
            }
            
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel", fontSize = 16.sp)
            }
        }
    }
}
