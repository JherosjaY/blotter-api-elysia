package com.example.blottermanagementsystem.ui.screens.sms

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.SmsNotification
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.SmsHelper
import com.example.blottermanagementsystem.viewmodel.SmsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSmsScreen(
    reportId: Int,
    respondentId: Int,
    respondentName: String,
    respondentNumber: String,
    caseNumber: String,
    onNavigateBack: () -> Unit,
    viewModel: SmsViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { PreferencesManager(context) }
    val currentUserRole = preferencesManager.role
    
    // ⚠️ SECURITY CHECK: Only Officers can send SMS
    val isOfficer = currentUserRole == "Officer"
    
    var selectedMessageType by remember { mutableStateOf("INITIAL_NOTICE") }
    var customMessage by remember { mutableStateOf("") }
    var showTypeMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSmsPermission by remember { mutableStateOf(SmsHelper.hasSmsPermission(context)) }
    
    // SMS Permission launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "SMS permission is required to send messages", Toast.LENGTH_LONG).show()
        }
    }
    
    // Generate message based on type
    val generatedMessage = remember(selectedMessageType) {
        when (selectedMessageType) {
            "INITIAL_NOTICE" -> SmsHelper.generateInitialNotice(
                caseNumber = caseNumber,
                respondentName = respondentName,
                accusation = "Blotter case filed against you"
            )
            "HEARING_NOTICE" -> SmsHelper.generateHearingNotice(
                caseNumber = caseNumber,
                respondentName = respondentName,
                hearingDate = "TBA",
                hearingTime = "TBA"
            )
            "REMINDER" -> SmsHelper.generateReminder(
                caseNumber = caseNumber,
                respondentName = respondentName,
                daysRemaining = 3
            )
            "FINAL_NOTICE" -> SmsHelper.generateFinalNotice(
                caseNumber = caseNumber,
                respondentName = respondentName
            )
            else -> ""
        }
    }
    
    val messageToSend = if (customMessage.isNotBlank()) customMessage else generatedMessage
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send SMS Notification", fontWeight = FontWeight.Bold) },
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
            // ⚠️ ROLE RESTRICTION WARNING
            if (!isOfficer) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "ACCESS DENIED",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Only OFFICERS can send SMS notifications.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your role: $currentUserRole",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
                return@Column // Stop rendering rest of the screen
            }
            
            // Recipient Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Recipient Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Name: $respondentName", fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Number: $respondentNumber", fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Case: $caseNumber", fontSize = 14.sp)
                    }
                }
            }
            
            // Message Type Selector
            ExposedDropdownMenuBox(
                expanded = showTypeMenu,
                onExpandedChange = { showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedMessageType.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Message Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showTypeMenu,
                    onDismissRequest = { showTypeMenu = false }
                ) {
                    listOf("INITIAL_NOTICE", "HEARING_NOTICE", "REMINDER", "FINAL_NOTICE").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.replace("_", " ")) },
                            onClick = {
                                selectedMessageType = type
                                showTypeMenu = false
                                customMessage = "" // Reset custom message
                            }
                        )
                    }
                }
            }
            
            // Message Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
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
                            "Message Preview",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                        Text(
                            "${messageToSend.length} chars",
                            fontSize = 12.sp,
                            color = if (messageToSend.length > 160) WarningOrange else SuccessGreen
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = customMessage.ifBlank { generatedMessage },
                        onValueChange = { customMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 8,
                        maxLines = 12,
                        placeholder = { Text("Edit message or use template...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    
                    if (messageToSend.length > 160) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Message will be sent as ${(messageToSend.length / 160) + 1} SMS (₱${(messageToSend.length / 160) + 1})",
                            fontSize = 12.sp,
                            color = WarningOrange
                        )
                    }
                }
            }
            
            // Permission Warning
            if (!hasSmsPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = WarningOrange)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "SMS Permission Required",
                                fontWeight = FontWeight.Bold,
                                color = WarningOrange
                            )
                            Text(
                                "Grant permission to send SMS",
                                fontSize = 12.sp
                            )
                        }
                        TextButton(
                            onClick = {
                                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }
            
            // Cost Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = ElectricBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "This will use your phone's SMS load (₱1 per 160 characters)",
                        fontSize = 12.sp
                    )
                }
            }
            
            // Officer Info Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Officer: ${preferencesManager.firstName} ${preferencesManager.lastName}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            "Authorized to send SMS notifications",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Send Button
            Button(
                onClick = {
                    // Double-check role (security)
                    if (!isOfficer) {
                        Toast.makeText(context, "Only Officers can send SMS", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    
                    if (!hasSmsPermission) {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                        return@Button
                    }
                    
                    if (messageToSend.isBlank()) {
                        Toast.makeText(context, "Message cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        try {
                            // Send SMS
                            val success = SmsHelper.sendSms(
                                phoneNumber = respondentNumber,
                                message = messageToSend,
                                onSuccess = {
                                    Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, "Failed: $error", Toast.LENGTH_LONG).show()
                                }
                            )
                            
                            if (success) {
                                // Save to database
                                val notification = SmsNotification(
                                    respondentId = respondentId,
                                    blotterReportId = reportId,
                                    messageType = selectedMessageType,
                                    messageContent = messageToSend,
                                    recipientNumber = respondentNumber,
                                    deliveryStatus = "Sent"
                                )
                                viewModel.sendNotification(notification)
                                
                                onNavigateBack()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && hasSmsPermission && messageToSend.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send SMS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
