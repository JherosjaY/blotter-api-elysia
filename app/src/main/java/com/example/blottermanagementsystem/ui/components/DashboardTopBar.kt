package com.example.blottermanagementsystem.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    title: String,
    subtitle: String,
    firstName: String,
    notificationCount: Int = 0,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val userRole = preferencesManager.userRole ?: "User"
    val profileImageUri = preferencesManager.profileImageUri
    val profileEmoji = preferencesManager.profileEmoji ?: "👤"
    
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Welcome Section
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome, $firstName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        actions = {
            // Right Section - Profile and Notifications (swapped)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile (moved first)
                IconButton(onClick = onNavigateToProfile) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show logo for Admin/Officer, profile picture for Users
                        if (userRole == "Admin" || userRole == "Officer") {
                            Icon(
                                imageVector = if (userRole == "Admin") Icons.Default.AdminPanelSettings else Icons.Default.Shield,
                                contentDescription = userRole,
                                tint = ElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
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
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
                
                // Notifications (moved second)
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge(
                                containerColor = DangerRed,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ) {
                                Text(
                                    text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = if (notificationCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (notificationCount > 0) DangerRed else ElectricBlue
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkNavy
        )
    )
}
