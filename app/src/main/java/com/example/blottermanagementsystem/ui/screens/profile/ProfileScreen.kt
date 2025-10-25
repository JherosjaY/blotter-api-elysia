package com.example.blottermanagementsystem.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
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
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFullImageDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val firstName = preferencesManager.firstName ?: "User"
    val lastName = preferencesManager.lastName ?: ""
    val username = preferencesManager.username ?: ""
    val role = preferencesManager.userRole ?: "User"
    val userId = preferencesManager.userId
    val profileImageUri = preferencesManager.profileImageUri
    val profileEmoji = preferencesManager.profileEmoji ?: "👤"
    
    // Admin/Officer has read-only profile
    val isAdmin = role == "Admin"
    val isOfficer = role == "Officer"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
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
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 16.dp  // Minimal bottom space
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = true
        ) {
            // Profile Header
            item(key = "profile_header") {
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
                        // Avatar (Show logo for Admin/Officer, profile picture for Users)
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue.copy(alpha = 0.2f))
                                .clickable(enabled = !isAdmin && !isOfficer && profileImageUri != null) {
                                    showFullImageDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAdmin || isOfficer) {
                                // Show icon for Admin/Officer
                                Icon(
                                    imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Shield,
                                    contentDescription = role,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(56.dp)
                                )
                            } else {
                                // Show profile picture/emoji for Users
                                if (profileImageUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(Uri.parse(profileImageUri)),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = profileEmoji,
                                        fontSize = 48.sp
                                    )
                                }
                            }
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
                    color = TextPrimary
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Default.Person,
                    label = "First Name",
                    value = firstName
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Default.Person,
                    label = "Last Name",
                    value = lastName
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Default.AccountCircle,
                    label = "Username",
                    value = username
                )
            }
            
            item {
                ProfileInfoCard(
                    icon = Icons.Default.Star,
                    label = "Role",
                    value = role
                )
            }
            
            // Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Only show Edit Profile for non-Admin users
            if (!isAdmin) {
                item {
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
            
            // Settings Button
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
            
            // Delete Account Button
            item {
                OutlinedButton(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, ErrorRed),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete My Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Divider with text
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DividerColor
                    )
                    Text(
                        text = "Explore the world?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = DividerColor
                    )
                }
            }
            
            // Logout Button
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
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editFirstName by remember { mutableStateOf(firstName) }
        var editLastName by remember { mutableStateOf(lastName) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    preferencesManager.profileImageUri = it.toString()
                    successMessage = "Photo updated!"
                } catch (e: Exception) {
                    errorMessage = "Failed to update photo"
                }
            }
        }
        
        AlertDialog(
            onDismissRequest = { 
                showEditProfileDialog = false
                errorMessage = null
                successMessage = null
            },
            title = {
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editFirstName,
                        onValueChange = { 
                            editFirstName = it
                            errorMessage = null
                        },
                        label = { Text("First Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = TextSecondary
                        )
                    )
                    
                    OutlinedTextField(
                        value = editLastName,
                        onValueChange = { 
                            editLastName = it
                            errorMessage = null
                        },
                        label = { Text("Last Name") },
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    successMessage?.let {
                        Text(
                            text = it,
                            color = SuccessGreen,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            editFirstName.isBlank() || editLastName.isBlank() -> {
                                errorMessage = "All fields are required"
                            }
                            else -> {
                                scope.launch {
                                    isLoading = true
                                    showEditProfileDialog = false
                                    
                                    val success = authViewModel.updateUserProfile(
                                        userId = userId,
                                        firstName = editFirstName,
                                        lastName = editLastName,
                                        username = username // Keep existing username
                                    )
                                    
                                    kotlinx.coroutines.delay(1500) // Show loading for at least 1.5s
                                    
                                    if (success) {
                                        preferencesManager.firstName = editFirstName
                                        preferencesManager.lastName = editLastName
                                        // Don't update username - it stays the same
                                    }
                                    
                                    isLoading = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Full Image Dialog
    if (showFullImageDialog && profileImageUri != null) {
        AlertDialog(
            onDismissRequest = { showFullImageDialog = false },
            title = null,
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(profileImageUri)),
                        contentDescription = "Full Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFullImageDialog = false }) {
                    Text("Close", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Change Password Dialog
    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmNewPassword by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false
                currentPassword = ""
                newPassword = ""
                confirmNewPassword = ""
                errorMessage = null
                successMessage = null
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
                    
                    successMessage?.let {
                        Text(
                            text = it,
                            color = SuccessGreen,
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
                                        successMessage = "Password changed successfully!"
                                        kotlinx.coroutines.delay(1500)
                                        showChangePasswordDialog = false
                                    } else {
                                        errorMessage = "Current password is incorrect"
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
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
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Logout",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to logout?",
                    color = TextSecondary,
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
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
    
    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        var deleteConfirmationText by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { 
                showDeleteAccountDialog = false
                deleteConfirmationText = ""
                errorMessage = ""
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Delete Account?",
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Once you delete your account, there is no going back. Please be certain.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Type DELETE MY ACCOUNT to confirm:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { 
                            deleteConfirmationText = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DELETE MY ACCOUNT") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ErrorRed,
                            unfocusedBorderColor = DividerColor,
                            cursorColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 12.sp,
                            color = ErrorRed,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmationText == "DELETE MY ACCOUNT") {
                            scope.launch {
                                isLoading = true
                                // Delete user from database
                                val success = userId?.let { authViewModel.deleteUserAccount(it) } ?: false
                                
                                if (success) {
                                    // Clear session and logout
                                    preferencesManager.clearSession()
                                    isLoading = false
                                    showDeleteAccountDialog = false
                                    onLogout()
                                } else {
                                    isLoading = false
                                    errorMessage = "Failed to delete account. Please try again."
                                }
                            }
                        } else {
                            errorMessage = "Please type DELETE MY ACCOUNT exactly"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    ),
                    enabled = deleteConfirmationText.isNotEmpty()
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteAccountDialog = false
                    deleteConfirmationText = ""
                    errorMessage = ""
                }) {
                    Text("Cancel", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Fullscreen Loading Overlay
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavy.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Rotating loading animation
                val infiniteTransition = rememberInfiniteTransition(label = "loading")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = ElectricBlue,
                        strokeWidth = 4.dp
                    )
                }
                
                Text(
                    text = "Updating Profile...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = "Please wait",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}
