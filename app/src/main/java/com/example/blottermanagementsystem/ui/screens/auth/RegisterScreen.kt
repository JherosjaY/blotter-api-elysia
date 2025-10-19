package com.example.blottermanagementsystem.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is com.example.blottermanagementsystem.viewmodel.AuthState.Success -> {
                onRegisterSuccess()
                viewModel.resetAuthState()
            }
            is com.example.blottermanagementsystem.viewmodel.AuthState.Error -> {
                errorMessage = (authState as com.example.blottermanagementsystem.viewmodel.AuthState.Error).message
            }
            else -> {}
        }
    }

    // ULTRA ANIMATIONS
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val orb1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )
    
    val orb2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -80f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )
    
    val orb3Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb3Rotation"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000),
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // ANIMATED BACKGROUND ORBS
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp, y = orb1Offset.dp)
                .alpha(glowAlpha * 0.4f)
                .blur(50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SuccessGreen.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = orb2Offset.dp)
                .alpha(glowAlpha * 0.3f)
                .blur(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 100.dp)
                .rotate(orb3Rotation)
                .alpha(glowAlpha * 0.25f)
                .blur(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            InfoBlue.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Join the Blotter Management System",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Register Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // First Name
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { 
                            firstName = it
                            errorMessage = null
                        },
                        label = { Text("First Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = ElectricBlue,
                            cursorColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Last Name
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { 
                            lastName = it
                            errorMessage = null
                        },
                        label = { Text("Last Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = ElectricBlue,
                            cursorColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = ElectricBlue,
                            cursorColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = ElectricBlue
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = ElectricBlue,
                            cursorColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password", maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                    tint = ElectricBlue
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = ElectricBlue,
                            cursorColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Error Message
                    AnimatedVisibility(visible = errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = ErrorRed,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Register Button
                    Button(
                        onClick = {
                            when {
                                firstName.isBlank() || lastName.isBlank() || 
                                username.isBlank() || password.isBlank() || 
                                confirmPassword.isBlank() -> {
                                    errorMessage = "Please fill in all fields"
                                }
                                password != confirmPassword -> {
                                    errorMessage = "Passwords do not match"
                                }
                                password.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                else -> {
                                    errorMessage = null
                                    // Public registration is always "Clerk" role
                                    viewModel.register(
                                        firstName = firstName,
                                        lastName = lastName,
                                        username = username,
                                        password = password,
                                        role = "Clerk"
                                    )
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
                        enabled = authState !is com.example.blottermanagementsystem.viewmodel.AuthState.Loading
                    ) {
                        if (authState is com.example.blottermanagementsystem.viewmodel.AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Register",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Login",
                        color = ElectricBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
