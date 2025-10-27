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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
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
    
    val firstName = preferencesManager.firstName ?: "Admin"
    val lastName = preferencesManager.lastName ?: ""
    val username = preferencesManager.username ?: ""
    val role = preferencesManager.userRole ?: "Admin"
    val userId = preferencesManager.userId
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Profile",
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
                        // Admin Icon
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
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
                AdminInfoCard(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = "$firstName $lastName"
                )
            }
            
            item {
                AdminInfoCard(
                    icon = Icons.Default.AccountCircle,
                    label = "Username",
                    value = username
                )
            }
            
            item {
                AdminInfoCard(
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
    
    // Enhanced Change Password Dialog with BCrypt + Cloud Sync
    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmNewPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var showCurrentPassword by remember { mutableStateOf(false) }
        var showNewPassword by remember { mutableStateOf(false) }
        var showConfirmPassword by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { 
                if (!isLoading) {
                    showChangePasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    errorMessage = null
                    successMessage = null
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Change Password",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Secured with BCrypt • Cloud Synced",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Current Password Field
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { 
                            currentPassword = it
                            errorMessage = null
                            successMessage = null
                        },
                        label = { Text("Current Password") },
                        placeholder = { Text("Enter current password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showCurrentPassword) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = ElectricBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // New Password Field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            errorMessage = null
                            successMessage = null
                        },
                        label = { Text("New Password") },
                        placeholder = { Text("Enter new password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showNewPassword) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SuccessGreen,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = SuccessGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Password Strength Indicator
                    if (newPassword.isNotEmpty()) {
                        val strength = when {
                            newPassword.length < 6 -> "Weak"
                            newPassword.length < 8 -> "Medium"
                            newPassword.length >= 8 && newPassword.any { it.isDigit() } && newPassword.any { it.isUpperCase() } -> "Strong"
                            else -> "Medium"
                        }
                        val strengthColor = when (strength) {
                            "Weak" -> ErrorRed
                            "Medium" -> WarningOrange
                            "Strong" -> SuccessGreen
                            else -> TextSecondary
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (strength) {
                                    "Weak" -> Icons.Default.Warning
                                    "Medium" -> Icons.Default.Info
                                    "Strong" -> Icons.Default.CheckCircle
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = strengthColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Password Strength: $strength",
                                fontSize = 12.sp,
                                color = strengthColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = { 
                            confirmNewPassword = it
                            errorMessage = null
                            successMessage = null
                        },
                        label = { Text("Confirm New Password") },
                        placeholder = { Text("Re-enter new password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (confirmNewPassword.isNotEmpty() && newPassword == confirmNewPassword) SuccessGreen else TextSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (confirmNewPassword.isNotEmpty() && newPassword == confirmNewPassword) SuccessGreen else ElectricBlue,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = if (confirmNewPassword.isNotEmpty() && newPassword == confirmNewPassword) SuccessGreen else ElectricBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Error Message
                    errorMessage?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = it,
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Success Message
                    successMessage?.let {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = it,
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Info Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = InfoBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Password will be encrypted with BCrypt and synced to cloud",
                                    color = InfoBlue,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                                Text(
                                    text = "• Multi-device sync enabled\n• Old password will be invalid on all devices",
                                    color = InfoBlue.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
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
                            currentPassword == newPassword -> {
                                errorMessage = "New password must be different from current password"
                            }
                            else -> {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val success = authViewModel.changePassword(
                                            userId = userId,
                                            currentPassword = currentPassword,
                                            newPassword = newPassword
                                        )
                                        if (success) {
                                            successMessage = "✅ Password changed successfully! Synced to cloud."
                                            kotlinx.coroutines.delay(2000)
                                            showChangePasswordDialog = false
                                        } else {
                                            errorMessage = "Current password is incorrect"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to change password: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Updating...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Password")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (!isLoading) {
                            showChangePasswordDialog = false
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Cancel", color = if (isLoading) TextSecondary else ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp)
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = TextPrimary
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
private fun AdminInfoCard(
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
