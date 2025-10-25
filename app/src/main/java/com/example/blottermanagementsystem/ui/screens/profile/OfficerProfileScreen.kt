package com.example.blottermanagementsystem.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    val firstName = preferencesManager.firstName ?: "Officer"
    val lastName = preferencesManager.lastName ?: ""
    val username = preferencesManager.username ?: ""
    val role = preferencesManager.userRole ?: "Officer"
    val userId = preferencesManager.userId
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Officer Profile",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Officer Icon
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Officer",
                                tint = ElectricBlue,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "$firstName $lastName",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "@$username",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = role,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
            
            // Account Information
            item {
                Text(
                    text = "Account Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                OfficerInfoCard(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = "$firstName $lastName"
                )
            }
            
            item {
                OfficerInfoCard(
                    icon = Icons.Default.AccountCircle,
                    label = "Username",
                    value = username
                )
            }
            
            item {
                OfficerInfoCard(
                    icon = Icons.Default.Star,
                    label = "Role",
                    value = role
                )
            }
            
            // Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                OutlinedButton(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricBlue
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Change Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = InfoBlue
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, InfoBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DividerColor
                    )
                    Text(
                        text = "Session",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DividerColor
                    )
                }
            }
            
            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmNewPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                errorMessage = null
            },
            title = {
                Text(
                    text = "Change Password",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { 
                            currentPassword = it
                            errorMessage = null
                        },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TextSecondary
                        )
                    )
                    
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TextSecondary
                        )
                    )
                    
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { 
                            confirmNewPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TextSecondary
                        )
                    )
                    
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty() -> {
                                errorMessage = "All fields are required"
                            }
                            newPassword != confirmNewPassword -> {
                                errorMessage = "New passwords do not match"
                            }
                            newPassword.length < 6 -> {
                                errorMessage = "Password must be at least 6 characters"
                            }
                            else -> {
                                scope.launch {
                                    val success = authViewModel.changePassword(
                                        userId = userId,
                                        currentPassword = currentPassword,
                                        newPassword = newPassword
                                    )
                                    if (success) {
                                        showChangePasswordDialog = false
                                    } else {
                                        errorMessage = "Current password is incorrect"
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Change Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout?",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun OfficerInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
