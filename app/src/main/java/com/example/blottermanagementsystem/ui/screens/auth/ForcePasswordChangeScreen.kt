package com.example.blottermanagementsystem.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForcePasswordChangeScreen(
    userId: Int,
    currentUsername: String,
    onPasswordChanged: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var newUsername by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                // Warning Icon
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WarningOrange,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Title
                Text(
                    text = "Change Your Credentials",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle
                Text(
                    text = "For security reasons, you must change your temporary credentials before accessing the system.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Current Username Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Current Username",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUsername,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // New Username Field
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { 
                        newUsername = it.lowercase().trim()
                        usernameError = when {
                            it.length < 4 -> "Username must be at least 4 characters"
                            it.contains(" ") -> "Username cannot contain spaces"
                            !it.matches(Regex("^[a-z0-9._]+$")) -> "Only lowercase letters, numbers, dots and underscores"
                            else -> null
                        }
                    },
                    label = { Text("New Username") },
                    leadingIcon = { 
                        Icon(Icons.Default.Person, null, tint = ElectricBlue) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = usernameError != null,
                    supportingText = {
                        if (usernameError != null) {
                            Text(usernameError!!, color = ErrorRed, fontSize = 12.sp)
                        } else {
                            Text("Choose a unique username", color = TextSecondary, fontSize = 12.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = ElectricBlue,
                        errorBorderColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // New Password Field
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        passwordError = when {
                            it.length < 6 -> "Password must be at least 6 characters"
                            else -> null
                        }
                    },
                    label = { Text("New Password") },
                    leadingIcon = { 
                        Icon(Icons.Default.Lock, null, tint = ElectricBlue) 
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) {
                            Text(passwordError!!, color = ErrorRed, fontSize = 12.sp)
                        } else {
                            Text("At least 6 characters", color = TextSecondary, fontSize = 12.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = ElectricBlue,
                        errorBorderColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        confirmError = if (it != newPassword && it.isNotEmpty()) {
                            "Passwords do not match"
                        } else null
                    },
                    label = { Text("Confirm Password") },
                    leadingIcon = { 
                        Icon(Icons.Default.Lock, null, tint = ElectricBlue) 
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmError != null,
                    supportingText = {
                        if (confirmError != null) {
                            Text(confirmError!!, color = ErrorRed, fontSize = 12.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = ElectricBlue,
                        errorBorderColor = ErrorRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Change Credentials Button
                Button(
                    onClick = {
                        if (newUsername.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (usernameError != null || passwordError != null || confirmError != null) {
                            Toast.makeText(context, "Please fix errors", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isLoading = true
                        scope.launch {
                            try {
                                val success = authViewModel.updateUserCredentials(
                                    userId = userId,
                                    newUsername = newUsername,
                                    newPassword = newPassword
                                )
                                
                                if (success) {
                                    // Clear the mustChangePassword flag
                                    val preferencesManager = com.example.blottermanagementsystem.utils.PreferencesManager(context)
                                    preferencesManager.mustChangePassword = false
                                    
                                    Toast.makeText(context, "Credentials updated successfully!", Toast.LENGTH_SHORT).show()
                                    onPasswordChanged()
                                } else {
                                    Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Update Credentials", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Security Note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = WarningOrange.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Important",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningOrange
                            )
                            Text(
                                text = "Remember your new credentials. You will use them for all future logins.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
