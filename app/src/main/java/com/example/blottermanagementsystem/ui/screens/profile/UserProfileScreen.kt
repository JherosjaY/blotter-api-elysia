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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
fun UserProfileScreen(
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
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val firstName = preferencesManager.firstName ?: "User"
    val lastName = preferencesManager.lastName ?: ""
    val username = preferencesManager.username ?: ""
    val role = preferencesManager.userRole ?: "User"
    val userId = preferencesManager.userId
    var profileImageUri by remember { mutableStateOf<String?>(null) }
    var profileEmoji by remember { mutableStateOf("👤") }
    
    // Load profile photo from database (not just preferences!)
    LaunchedEffect(userId) {
        scope.launch {
            val user = authViewModel.getUserById(userId)
            if (user != null && !user.profilePhotoUri.isNullOrEmpty()) {
                val photoUri = user.profilePhotoUri!!
                if (photoUri.startsWith("emoji:")) {
                    // It's an emoji
                    profileEmoji = photoUri.removePrefix("emoji:")
                    profileImageUri = null
                } else {
                    // It's an image URI
                    profileImageUri = photoUri
                }
            } else {
                // Fallback to preferences
                profileImageUri = preferencesManager.profileImageUri
                profileEmoji = preferencesManager.profileEmoji ?: "👤"
            }
        }
    }
    
    // Image picker launcher
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
                preferencesManager.hasSelectedProfilePicture = true
                // Update state immediately
                profileImageUri = it.toString()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
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
            // Profile Header with Photo
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
                        // Profile Photo with Edit Button
                        Box(
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue.copy(alpha = 0.2f))
                                    .clickable { showFullImageDialog = profileImageUri != null },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUri != null) {
                                    // Load image from URL, file path, or URI
                                    val imageUri = when {
                                        profileImageUri!!.startsWith("http://") || profileImageUri!!.startsWith("https://") -> {
                                            // It's a cloud URL (Cloudinary)
                                            profileImageUri
                                        }
                                        profileImageUri!!.startsWith("/") -> {
                                            // It's a file path
                                            Uri.fromFile(java.io.File(profileImageUri!!))
                                        }
                                        else -> {
                                            // It's a content URI
                                            Uri.parse(profileImageUri)
                                        }
                                    }
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUri),
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = profileEmoji,
                                        fontSize = 56.sp
                                    )
                                }
                            }
                            
                            // Edit Photo Button
                            FloatingActionButton(
                                onClick = { showPhotoOptionsDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.BottomEnd),
                                containerColor = ElectricBlue,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Photo",
                                    modifier = Modifier.size(20.dp)
                                )
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
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            item {
                UserInfoCard(
                    icon = Icons.Default.Person,
                    label = "First Name",
                    value = firstName
                )
            }
            
            item {
                UserInfoCard(
                    icon = Icons.Default.Person,
                    label = "Last Name",
                    value = lastName
                )
            }
            
            item {
                UserInfoCard(
                    icon = Icons.Default.AccountCircle,
                    label = "Username",
                    value = username
                )
            }
            
            // Actions
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
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
    
    // Photo Options Dialog
    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = {
                Text(
                    text = "Change Profile Photo",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showPhotoOptionsDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                    
                    if (profileImageUri != null) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    // Clear local preferences
                                    preferencesManager.profileImageUri = null
                                    profileImageUri = null
                                    profileEmoji = "👤" // Reset to default emoji
                                    
                                    // Sync to cloud - set empty string
                                    authViewModel.updateUserProfile(userId, "")
                                }
                                showPhotoOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            border = androidx.compose.foundation.BorderStroke(2.dp, ErrorRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Remove Photo")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
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
                    val imageUri = when {
                        profileImageUri!!.startsWith("http://") || profileImageUri!!.startsWith("https://") -> {
                            // It's a cloud URL (Cloudinary)
                            profileImageUri
                        }
                        profileImageUri!!.startsWith("/") -> {
                            // It's a file path
                            Uri.fromFile(java.io.File(profileImageUri!!))
                        }
                        else -> {
                            // It's a content URI
                            Uri.parse(profileImageUri)
                        }
                    }
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
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
    
    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editFirstName by remember { mutableStateOf(firstName) }
        var editLastName by remember { mutableStateOf(lastName) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                showEditProfileDialog = false
                errorMessage = null
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
                                    val success = authViewModel.updateUserProfile(
                                        userId = userId,
                                        firstName = editFirstName,
                                        lastName = editLastName,
                                        username = username
                                    )
                                    
                                    if (success) {
                                        preferencesManager.firstName = editFirstName
                                        preferencesManager.lastName = editLastName
                                        showEditProfileDialog = false
                                    } else {
                                        errorMessage = "Failed to update profile"
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
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
                    textAlign = TextAlign.Center,
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
    
    // Delete Account Dialog (simplified version)
    if (showDeleteAccountDialog) {
        var deleteConfirmation by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { 
                showDeleteAccountDialog = false
                deleteConfirmation = ""
            },
            title = {
                Text(
                    text = "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = "This action cannot be undone. Type DELETE MY ACCOUNT to confirm:",
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deleteConfirmation,
                        onValueChange = { deleteConfirmation = it },
                        placeholder = { Text("DELETE MY ACCOUNT") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deleteConfirmation == "DELETE MY ACCOUNT") {
                            scope.launch {
                                authViewModel.deleteUserAccount(userId)
                                onLogout()
                            }
                        }
                    },
                    enabled = deleteConfirmation == "DELETE MY ACCOUNT",
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = ElectricBlue)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun UserInfoCard(
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
