package com.example.blottermanagementsystem.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
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
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Preview with bounce animation
        val infiniteTransition = rememberInfiniteTransition(label = "bounce")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, ElectricBlue, CircleShape)
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
                    fontSize = 64.sp
                )
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
        
        // Grid of emoji options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            )
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(emojiOptions) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedEmoji == emoji && selectedImageUri == null) ElectricBlue.copy(alpha = 0.3f)
                                else SurfaceDark
                            )
                            .border(
                                width = if (selectedEmoji == emoji && selectedImageUri == null) 2.dp else 0.dp,
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
                    // Save selected profile picture
                    if (selectedImageUri != null) {
                        preferencesManager.profileImageUri = selectedImageUri.toString()
                    } else {
                        preferencesManager.profileEmoji = selectedEmoji
                    }
                    
                    // Mark as selected
                    preferencesManager.hasSelectedProfilePicture = true
                    
                    // Update user's profileCompleted flag in database
                    val userId = preferencesManager.userId
                    if (userId != -1) {
                        authViewModel.markProfileCompleted(userId)
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
