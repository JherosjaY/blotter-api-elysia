package com.example.blottermanagementsystem.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*

/**
 * Update Dialog - Notifies users of new app version
 * Shows "Update Now" or "Later" options
 */
@Composable
fun UpdateDialog(
    currentVersion: String,
    latestVersion: String,
    updateMessage: String,
    forceUpdate: Boolean = false,
    updateUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = { 
            if (!forceUpdate) {
                onDismiss()
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (forceUpdate) "Update Required" else "New Version Available!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Version info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = InfoBlue.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Current Version",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = currentVersion,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = InfoBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Latest Version",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = latestVersion,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
                
                // What's new
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "What's New:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateMessage,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
                
                // Force update message
                if (forceUpdate) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = WarningOrange.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "This update is required to continue using the app.",
                                fontSize = 12.sp,
                                color = WarningOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Open Play Store
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                    context.startActivity(intent)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Update Now",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!forceUpdate) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Later",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}
