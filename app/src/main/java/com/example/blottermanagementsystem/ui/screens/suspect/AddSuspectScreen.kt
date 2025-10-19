package com.example.blottermanagementsystem.ui.screens.suspect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Suspect
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSuspectScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    caseNumber: String = "",
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
    val userRole = preferencesManager.role
    
    var name by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showGenderMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val genders = listOf("Male", "Female", "Other")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Suspect", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Suspect Name *") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = ErrorRed) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text("Alias / Nickname") },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                    label = { Text("Age") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = ElectricBlue) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                ExposedDropdownMenuBox(
                    expanded = showGenderMenu,
                    onExpandedChange = { showGenderMenu = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showGenderMenu) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showGenderMenu,
                        onDismissRequest = { showGenderMenu = false }
                    ) {
                        genders.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    gender = g
                                    showGenderMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Last Known Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Physical Description") },
                placeholder = { Text("Height, build, distinguishing marks, etc.") },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = ElectricBlue) },
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
            
            Button(
                onClick = {
                    when {
                        name.isBlank() -> errorMessage = "Name is required"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val suspect = Suspect(
                                        blotterReportId = reportId,
                                        name = name,
                                        alias = alias.ifBlank { null },
                                        age = age.toIntOrNull(),
                                        gender = gender,
                                        address = address.ifBlank { null },
                                        description = description.ifBlank { null }
                                    )
                                    viewModel.addSuspect(suspect, currentUserName, caseNumber, userRole)
                                    onSaveSuccess()
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
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Suspect")
                }
            }
        }
    }
}
