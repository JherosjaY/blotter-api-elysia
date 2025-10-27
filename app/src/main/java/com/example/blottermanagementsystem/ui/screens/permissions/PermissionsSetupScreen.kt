package com.example.blottermanagementsystem.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.utils.PreferencesManager

/**
 * Permissions Setup Screen
 * Shows on first app launch to request all necessary permissions
 */
@Composable
fun PermissionsSetupScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    // Define all permissions we need
    val permissions = remember {
        buildList {
            // Notifications (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            // Camera
            add(Manifest.permission.CAMERA)
            // Photos and Videos
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            // SMS
            add(Manifest.permission.SEND_SMS)
            add(Manifest.permission.READ_SMS)
            // Microphone
            add(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        // Check if all permissions are granted
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            // Save that permissions were granted
            preferencesManager.permissionsGranted = true
            onPermissionsGranted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Icon
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Permissions",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "App Permissions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "To provide you with the best experience, we need access to the following permissions:",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permission items
            PermissionItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Receive important updates about your cases and hearings"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = Icons.Default.CameraAlt,
                title = "Camera",
                description = "Take photos for evidence and documentation"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = Icons.Default.Photo,
                title = "Photos and Videos",
                description = "Upload images from your gallery as evidence"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = Icons.Default.Sms,
                title = "SMS",
                description = "Send notifications via text message"
            )

            Spacer(modifier = Modifier.height(16.dp))

            PermissionItem(
                icon = Icons.Default.Mic,
                title = "Microphone",
                description = "Record audio statements and evidence"
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Allow All Button
            Button(
                onClick = {
                    permissionLauncher.launch(permissions.toTypedArray())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Allow All Permissions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip button (not recommended)
            TextButton(
                onClick = {
                    // Mark as granted even if skipped (to avoid showing again)
                    preferencesManager.permissionsGranted = true
                    onPermissionsGranted()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for now",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
