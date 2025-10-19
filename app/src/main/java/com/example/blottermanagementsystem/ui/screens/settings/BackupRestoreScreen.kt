package com.example.blottermanagementsystem.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.DatabaseBackupManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val backupManager = remember { DatabaseBackupManager(context) }
    val scope = rememberCoroutineScope()
    
    var backups by remember { mutableStateOf<List<File>>(emptyList()) }
    var isCreatingBackup by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedBackup by remember { mutableStateOf<File?>(null) }
    
    LaunchedEffect(Unit) {
        backups = backupManager.getAvailableBackups()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = InfoBlue.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = InfoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Regular backups protect your data. Keep backups in a safe location.",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Create Backup Button
            item {
                Button(
                    onClick = {
                        isCreatingBackup = true
                        scope.launch {
                            backupManager.createBackup()
                            backups = backupManager.getAvailableBackups()
                            isCreatingBackup = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    enabled = !isCreatingBackup
                ) {
                    if (isCreatingBackup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Backup Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Export Button
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            backupManager.exportToExternalStorage()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export to Downloads", fontSize = 16.sp)
                }
            }
            
            // Available Backups
            item {
                Text(
                    "Available Backups (${backups.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            if (backups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.FolderOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No backups found", color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                items(backups) { backup ->
                    BackupCard(
                        backup = backup,
                        backupManager = backupManager,
                        onRestore = {
                            selectedBackup = backup
                            showRestoreDialog = true
                        },
                        onDelete = {
                            scope.launch {
                                backup.delete()
                                backups = backupManager.getAvailableBackups()
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Restore Confirmation Dialog
    if (showRestoreDialog && selectedBackup != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Database?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will replace your current database with the backup. " +
                    "The app will need to restart. Continue?",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            backupManager.restoreBackup(selectedBackup!!)
                            showRestoreDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningYellow)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BackupCard(
    backup: File,
    backupManager: DatabaseBackupManager,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        backup.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatDate(backup.lastModified()),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Size: ${backupManager.getBackupSize(backup)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Backup?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
