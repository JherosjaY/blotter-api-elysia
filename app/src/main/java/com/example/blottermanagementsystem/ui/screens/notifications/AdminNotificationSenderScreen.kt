package com.example.blottermanagementsystem.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import com.example.blottermanagementsystem.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationSenderScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var notificationTitle by remember { mutableStateOf("") }
    var notificationMessage by remember { mutableStateOf("") }
    var selectedRecipientType by remember { mutableStateOf("All Users") }
    var selectedUsers by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showUserSelector by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var allUsers by remember { mutableStateOf<List<com.example.blottermanagementsystem.data.entity.User>>(emptyList()) }
    
    // Load all users from cloud API
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                Log.d("AdminNotificationSender", "👥 Loading users from cloud API...")
                // Try to get from API first
                val apiRepository = com.example.blottermanagementsystem.data.repository.ApiRepository()
                val result = apiRepository.getAllUsersFromCloud()
                
                if (result.isSuccess) {
                    allUsers = result.getOrNull() ?: emptyList()
                    Log.d("AdminNotificationSender", "✅ Loaded ${allUsers.size} users from cloud")
                } else {
                    // Fallback to local database
                    Log.w("AdminNotificationSender", "⚠️ API failed, using local database")
                    allUsers = authViewModel.getAllUsersSync()
                }
            } catch (e: Exception) {
                Log.e("AdminNotificationSender", "❌ Error loading users: ${e.message}", e)
                // Fallback to local database
                allUsers = authViewModel.getAllUsersSync()
            }
        }
    }
    
    val regularUsers = remember(allUsers) { allUsers.filter { user -> user.role == "User" } }
    
    val recipientTypes = listOf("All Users", "Specific Users", "All Admins", "All Officers")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Send Notification",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ElectricBlue.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Push Notification",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Send announcements to users",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Recipient Type Selector
            item {
                Text(
                    text = "Recipients",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        recipientTypes.forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRecipientType = type
                                        if (type != "Specific Users") {
                                            selectedUsers = emptySet()
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRecipientType == type,
                                    onClick = {
                                        selectedRecipientType = type
                                        if (type != "Specific Users") {
                                            selectedUsers = emptySet()
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ElectricBlue
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = type,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
            
            // User Selector (if Specific Users selected)
            if (selectedRecipientType == "Specific Users") {
                item {
                    Button(
                        onClick = { showUserSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedUsers.isEmpty()) 
                                "Select Users" 
                            else 
                                "${selectedUsers.size} users selected"
                        )
                    }
                }
            }
            
            // Notification Title
            item {
                Text(
                    text = "Notification Title",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            item {
                OutlinedTextField(
                    value = notificationTitle,
                    onValueChange = { 
                        notificationTitle = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter notification title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Notification Message
            item {
                Text(
                    text = "Message",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            item {
                OutlinedTextField(
                    value = notificationMessage,
                    onValueChange = { 
                        notificationMessage = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("Enter notification message") },
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Error Message
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 13.sp,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }
            
            // Send Button
            item {
                Button(
                    onClick = {
                        when {
                            notificationTitle.isBlank() -> {
                                errorMessage = "Please enter a notification title"
                            }
                            notificationMessage.isBlank() -> {
                                errorMessage = "Please enter a notification message"
                            }
                            selectedRecipientType == "Specific Users" && selectedUsers.isEmpty() -> {
                                errorMessage = "Please select at least one user"
                            }
                            else -> {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    try {
                                        val success = notificationViewModel.sendManualNotification(
                                            title = notificationTitle,
                                            message = notificationMessage,
                                            recipientType = selectedRecipientType,
                                            specificUserIds = selectedUsers.toList()
                                        )
                                        
                                        if (success) {
                                            showSuccessDialog = true
                                            // Reset form
                                            notificationTitle = ""
                                            notificationMessage = ""
                                            selectedUsers = emptySet()
                                        } else {
                                            errorMessage = "Failed to send notification"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Notification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // User Selector Dialog
    if (showUserSelector) {
        AlertDialog(
            onDismissRequest = { showUserSelector = false },
            title = {
                Text(
                    text = "Select Users",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = regularUsers,
                        key = { user -> user.id }
                    ) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUsers = if (selectedUsers.contains(user.id)) {
                                        selectedUsers - user.id
                                    } else {
                                        selectedUsers + user.id
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedUsers.contains(user.id),
                                onCheckedChange = { isChecked ->
                                    selectedUsers = if (isChecked) {
                                        selectedUsers + user.id
                                    } else {
                                        selectedUsers - user.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ElectricBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${user.firstName} ${user.lastName}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@${user.username}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showUserSelector = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserSelector = false }) {
                    Text("Cancel", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Notification Sent!",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Your notification has been sent successfully to the selected recipients.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("OK")
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
