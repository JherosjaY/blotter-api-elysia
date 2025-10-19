package com.example.blottermanagementsystem.ui.screens.settings

import android.app.Activity
import android.media.RingtoneManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackupRestore: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    
    var showVersionDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = true
        ) {
            // Notifications Section
            item {
                Text(
                    text = "Notifications",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                SettingCard {
                    SettingSwitchItem(
                        title = "Enable Notifications",
                        description = "Receive notifications for updates",
                        icon = Icons.Default.Notifications,
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            Toast.makeText(
                                context,
                                if (enabled) "Notifications enabled" else "Notifications disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            
            item {
                SettingCard {
                    SettingSwitchItem(
                        title = "Sound",
                        description = "Play sound for notifications",
                        icon = Icons.Default.Star,
                        checked = soundEnabled,
                        onCheckedChange = { enabled ->
                            soundEnabled = enabled
                            
                            // Play notification sound when enabled
                            if (enabled) {
                                try {
                                    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                                    val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                                    ringtone.play()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            
                            Toast.makeText(
                                context,
                                if (enabled) "Sound enabled" else "Sound disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            
            // Data Management Section
            item {
                Text(
                    text = "Data Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                SettingCard {
                    SettingItem(
                        title = "Backup & Restore",
                        description = "Manage database backups",
                        icon = Icons.Default.Backup,
                        onClick = onNavigateToBackupRestore
                    )
                }
            }
            
            // About Section
            item {
                Text(
                    text = "About",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            item {
                SettingCard {
                    Column {
                        SettingItem(
                            title = "Version",
                            description = "1.0.0",
                            icon = Icons.Default.Info,
                            onClick = { showVersionDialog = true }
                        )
                        Divider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                        SettingItem(
                            title = "Privacy Policy",
                            description = "View privacy policy",
                            icon = Icons.Default.Lock,
                            onClick = { showPrivacyDialog = true }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Version Dialog
    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = InfoBlue,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "About Blotter Management System (B.M.S)",
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column {
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Barangay Blotter Management System",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "© 2025 All Rights Reserved",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A comprehensive system for managing barangay blotter reports, case tracking, and legal documentation.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVersionDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricBlue
                    )
                ) {
                    Text("Close")
                }
            },
            containerColor = CardBackground
        )
    }
    
    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Privacy Policy",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    item {
                        Text(
                            text = "Blotter Management System Privacy Policy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "1. Data Collection",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We collect only necessary information for case management.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "2. Data Storage",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All data is stored locally on your device using encrypted storage.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "3. Data Security",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We implement industry-standard security measures.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Last Updated: October 2025",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    Text("I Understand")
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ElectricBlue,
                checkedTrackColor = ElectricBlue.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}
