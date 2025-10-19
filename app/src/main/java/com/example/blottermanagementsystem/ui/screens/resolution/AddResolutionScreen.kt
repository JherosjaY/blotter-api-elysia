package com.example.blottermanagementsystem.ui.screens.resolution

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
import com.example.blottermanagementsystem.data.entity.Resolution
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResolutionScreen(
    reportId: Int,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    caseNumber: String = "",
    viewModel: DashboardViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val currentUserName = "${preferencesManager.firstName} ${preferencesManager.lastName}"
    
    var resolutionType by remember { mutableStateOf("Settled") }
    var details by remember { mutableStateOf("") }
    var actionTaken by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showTypeMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val resolutionTypes = listOf(
        "Settled",
        "Dismissed",
        "Referred to Higher Authority",
        "Mediated",
        "Withdrawn",
        "Other"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Close Case",
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
                    containerColor = SuccessGreen.copy(alpha = 0.15f)
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
                        Icons.Default.CheckCircle,
                        null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Closing this case",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            "This will mark the case as Resolved and archive it",
                            fontSize = 12.sp,
                            color = SuccessGreen.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Resolution Type
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = resolutionType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Resolution Type *") },
                    leadingIcon = { Icon(Icons.Default.Category, null, tint = ElectricBlue) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    resolutionTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                resolutionType = type
                                showTypeMenu = false
                            }
                        )
                    }
                }
            }
            
            // Details
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Resolution Details *") },
                placeholder = { Text("Describe how the case was resolved") },
                leadingIcon = { Icon(Icons.Default.Description, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Action Taken
            OutlinedTextField(
                value = actionTaken,
                onValueChange = { actionTaken = it },
                label = { Text("Action Taken *") },
                placeholder = { Text("What actions were taken to resolve this?") },
                leadingIcon = { Icon(Icons.Default.Task, null, tint = ElectricBlue) },
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
                        details.isBlank() -> errorMessage = "Details are required"
                        actionTaken.isBlank() -> errorMessage = "Action taken is required"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val resolution = Resolution(
                                        blotterReportId = reportId,
                                        resolutionType = resolutionType,
                                        resolutionDetails = details,
                                        resolvedBy = preferencesManager.userId ?: 0
                                    )
                                    viewModel.addResolution(resolution, currentUserName, caseNumber)
                                    
                                    // Update report status to Resolved
                                    adminViewModel.updateReportStatus(
                                        reportId, 
                                        "Resolved", 
                                        "Case closed: $resolutionType", 
                                        preferencesManager.userId ?: 0
                                    )
                                    
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
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextPrimary)
                } else {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Close Case")
                }
            }
        }
    }
}
