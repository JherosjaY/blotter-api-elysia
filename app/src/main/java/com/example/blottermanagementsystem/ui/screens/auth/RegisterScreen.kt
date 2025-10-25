package com.example.blottermanagementsystem.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
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

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    
    return when {
        score <= 2 -> PasswordStrength.WEAK
        score <= 3 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}

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
    
    var firstNameFocused by remember { mutableStateOf(false) }
    var lastNameFocused by remember { mutableStateOf(false) }
    var usernameFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var confirmPasswordFocused by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    val passwordStrength = remember(password) {
        if (password.isEmpty()) null else calculatePasswordStrength(password)
    }
    
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

    // ENHANCED ANIMATIONS - Simplified for performance
    
    // Entry animations
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        kotlinx.coroutines.delay(100)
        startAnimation = true 
    }
    
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val cardScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cardScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0f172a),
                        Color(0xFF1e293b),
                        Color(0xFF0f172a)
                    )
                )
            )
    ) {
        // ANIMATED PARTICLES
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-70).dp, y = 50.dp)
                .alpha(0.3f)
                .blur(75.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = 0.dp)
                .alpha(0.25f)
                .blur(65.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            InfoBlue.copy(alpha = 0.6f),
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
                .offset(y = 120.dp)
                .alpha(0.2f)
                .blur(55.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF60a5fa).copy(alpha = 0.5f),
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // ANIMATED LOGO
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800))
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Removed glow animation for performance
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        ElectricBlue.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(85.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        ElectricBlue.copy(alpha = 0.2f),
                                        InfoBlue.copy(alpha = 0.2f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 48.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TITLE
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, delayMillis = 200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Join the Blotter Management System",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // GLASSMORPHISM REGISTER CARD
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, delayMillis = 400)) + 
                        slideInVertically(tween(1000, delayMillis = 400)) { it / 2 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(cardScale),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // First Name
                            GlowTextField(
                                value = firstName,
                                onValueChange = { 
                                    firstName = it
                                    errorMessage = null
                                },
                                label = "First Name",
                                icon = Icons.Default.Person,
                                isFocused = firstNameFocused,
                                onFocusChanged = { firstNameFocused = it },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Last Name
                            GlowTextField(
                                value = lastName,
                                onValueChange = { 
                                    lastName = it
                                    errorMessage = null
                                },
                                label = "Last Name",
                                icon = Icons.Default.Person,
                                isFocused = lastNameFocused,
                                onFocusChanged = { lastNameFocused = it },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Username
                            GlowTextField(
                                value = username,
                                onValueChange = { 
                                    username = it
                                    errorMessage = null
                                },
                                label = "Username",
                                icon = Icons.Default.AccountCircle,
                                isFocused = usernameFocused,
                                onFocusChanged = { usernameFocused = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Password
                            GlowPasswordField(
                                value = password,
                                onValueChange = { 
                                    password = it
                                    errorMessage = null
                                },
                                label = "Password",
                                passwordVisible = passwordVisible,
                                onToggleVisibility = { passwordVisible = !passwordVisible },
                                isFocused = passwordFocused,
                                onFocusChanged = { passwordFocused = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                            
                            // Password Strength Indicator
                            AnimatedVisibility(visible = password.isNotEmpty()) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        repeat(3) { index ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(
                                                        when {
                                                            passwordStrength == null -> Color.White.copy(alpha = 0.2f)
                                                            index == 0 -> when (passwordStrength) {
                                                                PasswordStrength.WEAK -> ErrorRed
                                                                PasswordStrength.MEDIUM -> WarningOrange
                                                                PasswordStrength.STRONG -> SuccessGreen
                                                            }
                                                            index == 1 && passwordStrength != PasswordStrength.WEAK -> 
                                                                if (passwordStrength == PasswordStrength.MEDIUM) WarningOrange else SuccessGreen
                                                            index == 2 && passwordStrength == PasswordStrength.STRONG -> SuccessGreen
                                                            else -> Color.White.copy(alpha = 0.2f)
                                                        }
                                                    )
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text(
                                        text = when (passwordStrength) {
                                            PasswordStrength.WEAK -> "Weak password"
                                            PasswordStrength.MEDIUM -> "Medium password"
                                            PasswordStrength.STRONG -> "Strong password"
                                            null -> ""
                                        },
                                        fontSize = 12.sp,
                                        color = when (passwordStrength) {
                                            PasswordStrength.WEAK -> ErrorRed
                                            PasswordStrength.MEDIUM -> WarningOrange
                                            PasswordStrength.STRONG -> SuccessGreen
                                            null -> Color.Transparent
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Confirm Password
                            GlowPasswordField(
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                label = "Confirm Pass",
                                passwordVisible = confirmPasswordVisible,
                                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                                isFocused = confirmPasswordFocused,
                                onFocusChanged = { confirmPasswordFocused = it },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { 
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                            
                            // Password match indicator
                            AnimatedVisibility(
                                visible = confirmPassword.isNotEmpty() && password.isNotEmpty()
                            ) {
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (password == confirmPassword) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (password == confirmPassword) SuccessGreen else ErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (password == confirmPassword) "Passwords match" else "Passwords don't match",
                                        fontSize = 12.sp,
                                        color = if (password == confirmPassword) SuccessGreen else ErrorRed
                                    )
                                }
                            }
                            
                            // Error Message
                            AnimatedVisibility(
                                visible = errorMessage != null,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                Row(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        color = ErrorRed,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            // REGISTER BUTTON
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
                                            viewModel.register(firstName, lastName, username, password, "User")
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue
                                ),
                                shape = RoundedCornerShape(16.dp),
                                enabled = authState !is com.example.blottermanagementsystem.viewmodel.AuthState.Loading,
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 8.dp,
                                    pressedElevation = 12.dp
                                )
                            ) {
                                if (authState is com.example.blottermanagementsystem.viewmodel.AuthState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Create Account",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login Link
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, delayMillis = 600))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Login",
                            color = ElectricBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        if (isFocused) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp)
                    .background(
                        ElectricBlue.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    )
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = ElectricBlue
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedLabelColor = ElectricBlue,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                cursorColor = ElectricBlue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun GlowPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(14.dp))
    ) {
        if (isFocused) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp)
                    .background(
                        ElectricBlue.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    )
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = label,
                    tint = ElectricBlue
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedLabelColor = ElectricBlue,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                cursorColor = ElectricBlue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(14.dp)
        )
    }
}
