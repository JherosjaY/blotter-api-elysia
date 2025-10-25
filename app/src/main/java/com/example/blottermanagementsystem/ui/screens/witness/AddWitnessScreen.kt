package com.example.blottermanagementsystem.ui.screens.witness

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Witness
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWitnessScreen(
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
    var contactNumber by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var statement by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Phone validation function
    fun validatePhoneNumber(phone: String): Boolean {
        val pattern09 = "^09\\d{9}$".toRegex()
        return pattern09.matches(phone)
    }
    
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add Witness",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Witness Information",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            "Record witness details and their statement",
                            fontSize = 12.sp,
                            color = SuccessGreen.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Witness Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Witness Name *") },
                placeholder = { Text("Full name of witness") },
                leadingIcon = {
                    Icon(Icons.Default.Person, null, tint = SuccessGreen)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SuccessGreen,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = SuccessGreen
                )
            )
            
            // Contact Number
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
                label = { Text("Contact Number *") },
                placeholder = { Text("09XXXXXXXXX") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, null, tint = SuccessGreen)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                    focusedBorderColor = SuccessGreen,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = SuccessGreen,
                    errorBorderColor = ErrorRed
                )
            )
            
            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address *") },
                placeholder = { Text("Complete address") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, null, tint = SuccessGreen)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SuccessGreen,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = SuccessGreen
                )
            )
            
            // Witness Statement
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Witness Statement",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                    
                    OutlinedTextField(
                        value = statement,
                        onValueChange = { statement = it },
                        placeholder = { Text("What did the witness see/hear?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (name.isBlank() || contactNumber.isBlank() || address.isBlank()) {
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        val witness = Witness(
                            blotterReportId = reportId,
                            name = name,
                            contactNumber = contactNumber,
                            address = address,
                            statement = statement.ifBlank { null }
                        )
                        
                        viewModel.addWitness(witness, currentUserName, caseNumber, userRole)
                        isLoading = false
                        onSaveSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = androidx.compose.ui.graphics.Color.White
                    )
                } else {
                    Icon(
                        Icons.Default.Save,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save Witness",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}
