package com.example.blottermanagementsystem.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.viewmodel.ConnectedDevice
import com.example.blottermanagementsystem.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectedDevicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val devices by viewModel.connectedDevices.collectAsState()
    val sendStatus by viewModel.sendStatus.collectAsState()
    
    var showSendDialog by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<ConnectedDevice?>(null) }
    var showSendAllDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadConnectedDevices()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connected Devices") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadConnectedDevices() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (devices.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showSendAllDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Send, "Send to All")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status message
            sendStatus?.let { status ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.contains("✅")) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Device count
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 Connected Devices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${devices.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Devices list
            if (devices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No connected devices",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Users need to login to appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(devices) { device ->
                        DeviceCard(
                            device = device,
                            onSendClick = {
                                selectedDevice = device
                                showSendDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Send notification dialog
    if (showSendDialog && selectedDevice != null) {
        SendNotificationDialog(
            deviceName = "${selectedDevice!!.firstName} ${selectedDevice!!.lastName}",
            onDismiss = { showSendDialog = false },
            onSend = { title, message ->
                viewModel.sendNotificationToUser(selectedDevice!!.id, title, message)
                showSendDialog = false
            }
        )
    }
    
    // Send to all dialog
    if (showSendAllDialog) {
        SendNotificationDialog(
            deviceName = "All Users (${devices.size})",
            onDismiss = { showSendAllDialog = false },
            onSend = { title, message ->
                viewModel.sendNotificationToAll(title, message)
                showSendAllDialog = false
            }
        )
    }
}

@Composable
fun DeviceCard(
    device: ConnectedDevice,
    onSendClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${device.firstName.first()}${device.lastName.first()}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Device info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${device.firstName} ${device.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${device.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (device.role) {
                            "Admin" -> Color(0xFFFF6B6B)
                            "Officer" -> Color(0xFF4ECDC4)
                            else -> Color(0xFF95E1D3)
                        }
                    ) {
                        Text(
                            text = device.role,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                    
                    // Device model
                    device.deviceId?.let {
                        Text(
                            text = "📱 $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Online status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Text(
                        text = "Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // Send button
            IconButton(
                onClick = onSendClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send Notification",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SendNotificationDialog(
    deviceName: String,
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Send Notification")
                Text(
                    text = "To: $deviceName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Hey there!") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    placeholder = { Text("This is a test notification! 😄") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        onSend(title, message)
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
