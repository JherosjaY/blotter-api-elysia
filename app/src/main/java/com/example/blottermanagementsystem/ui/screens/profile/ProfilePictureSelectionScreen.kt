package com.example.blottermanagementsystem.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun ProfilePictureSelectionScreen(
    onComplete: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEmoji by remember { mutableStateOf("👤") }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Grant persistent permission
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }
    
    val emojiOptions = listOf(
        "👤", "😊", "😎", "🤓", "😇", "🥳",
        "🤠", "👨‍💼", "👩‍💼", "👨‍⚖️", "👩‍⚖️", "👮‍♂️",
        "👮‍♀️", "🧑‍💻", "👨‍🔧", "👩‍🔧", "🦸‍♂️", "🦸‍♀️",
        "🧙‍♂️", "🧙‍♀️", "🧑‍🎓", "👨‍🎓", "👩‍🎓", "🧑‍⚕️"
    )

    // Entry animation
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        startAnimation = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0f172a),
                        Color(0xFF1e293b)
                    )
                )
            )
    ) {
        // Background blur circles
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-50).dp)
                .blur(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
        
            // Title with animation
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Choose Your Avatar",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Select an emoji to represent your profile",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        
        Spacer(modifier = Modifier.height(40.dp))
        
            // Preview - removed bounce animation for performance
        
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800))
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, ElectricBlue, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                } else {
                    Text(
                        text = selectedEmoji,
                        fontSize = 72.sp
                    )
                }
                }
            }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Upload from Gallery Button
        OutlinedButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ElectricBlue
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, ElectricBlue),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📷",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload from Gallery",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
            // Grid of emoji options with glassmorphism
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1e293b).copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                    items(emojiOptions) { emoji ->
                        val isSelected = selectedEmoji == emoji && selectedImageUri == null
                        val scale by animateFloatAsState(
                            targetValue = if (isSelected) 1.1f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "emojiScale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) 
                                        Brush.linearGradient(
                                            colors = listOf(
                                                ElectricBlue.copy(alpha = 0.4f),
                                                InfoBlue.copy(alpha = 0.4f)
                                            )
                                        )
                                    else 
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF334155),
                                                Color(0xFF1e293b)
                                            )
                                        )
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = ElectricBlue,
                                    shape = CircleShape
                                )
                                .clickable { 
                                    selectedEmoji = emoji
                                    selectedImageUri = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 32.sp
                            )
                        }
                    }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Continue Button
        Button(
            onClick = {
                scope.launch {
                    val userId = preferencesManager.userId
                    
                    // Save selected profile picture to database AND preferences
                    val profilePhotoUri = if (selectedImageUri != null) {
                        // Upload image to Cloudinary
                        val uploadResult = com.example.blottermanagementsystem.utils.CloudinaryUploader.uploadImage(
                            context, selectedImageUri!!, userId
                        )
                        
                        if (uploadResult.isSuccess) {
                            uploadResult.getOrNull() ?: selectedImageUri.toString()
                        } else {
                            // Fallback: copy to internal storage if upload fails
                            val copiedPath = com.example.blottermanagementsystem.utils.ImageUtils.copyImageToInternalStorage(
                                context, selectedImageUri!!, userId
                            )
                            copiedPath ?: selectedImageUri.toString()
                        }
                    } else {
                        "emoji:$selectedEmoji" // Save emoji with prefix
                    }
                    
                    // Save to preferences (for immediate use)
                    if (selectedImageUri != null) {
                        preferencesManager.profileImageUri = profilePhotoUri
                    } else {
                        preferencesManager.profileEmoji = selectedEmoji
                    }
                    
                    // Mark as selected
                    preferencesManager.hasSelectedProfilePicture = true
                    
                    // Update user's profile in database (will sync to cloud)
                    if (userId != -1) {
                        authViewModel.updateUserProfile(userId, profilePhotoUri)
                    }
                    
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBlue
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
