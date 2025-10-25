package com.example.blottermanagementsystem.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var usernameFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is com.example.blottermanagementsystem.viewmodel.AuthState.Success -> {
                val user = (authState as com.example.blottermanagementsystem.viewmodel.AuthState.Success).user
                onLoginSuccess(user.role)
                viewModel.resetAuthState()
            }
            is com.example.blottermanagementsystem.viewmodel.AuthState.Error -> {
                errorMessage = (authState as com.example.blottermanagementsystem.viewmodel.AuthState.Error).message
            }
            else -> {}
        }
    }

    // ENHANCED ANIMATIONS
    // Simplified - removed heavy animations for performance
    
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
                        Color(0xFF0f172a), // Dark Navy
                        Color(0xFF1e293b), // Navy
                    )
                )
            )
    ) {
        
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = 50.dp)
                .alpha(0.3f)
                .blur(80.dp)
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
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = 0.dp)
                .alpha(0.25f)
                .blur(70.dp)
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ANIMATED LOGO WITH GLOW
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800))
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Removed glow animation for performance
                    
                    // Logo container
                    Box(
                        modifier = Modifier
                            .size(100.dp)
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
                            text = "🔐",
                            fontSize = 56.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // TITLE WITH GRADIENT
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, delayMillis = 200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Blotter Management",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "System",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Sign in to continue",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // GLASSMORPHISM LOGIN CARD
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
                            .padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Username Field with glow on focus
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(14.dp))
                            ) {
                                if (usernameFocused) {
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
                                    value = username,
                                    onValueChange = { 
                                        username = it
                                        errorMessage = null
                                    },
                                    label = { Text("Username", color = Color.White.copy(alpha = 0.7f)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Username",
                                            tint = ElectricBlue
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { usernameFocused = it.isFocused },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
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
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Password Field with glow on focus
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(14.dp))
                            ) {
                                if (passwordFocused) {
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
                                    value = password,
                                    onValueChange = { 
                                        password = it
                                        errorMessage = null
                                    },
                                    label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Password",
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { passwordFocused = it.isFocused },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { 
                                            focusManager.clearFocus()
                                            if (username.isNotBlank() && password.isNotBlank()) {
                                                viewModel.login(username, password)
                                            }
                                        }
                                    ),
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
                            
                            // Error Message with animation
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
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // ANIMATED LOGIN BUTTON
                            Button(
                                onClick = {
                                    if (username.isBlank() || password.isBlank()) {
                                        errorMessage = "Please fill in all fields"
                                    } else {
                                        errorMessage = null
                                        viewModel.login(username, password)
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
                                            text = "Login",
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Register Link
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, delayMillis = 600))
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Register",
                            color = ElectricBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
