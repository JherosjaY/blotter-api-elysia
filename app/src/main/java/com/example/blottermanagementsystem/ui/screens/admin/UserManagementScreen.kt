package com.example.blottermanagementsystem.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.User
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateBack: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val users by adminViewModel.allUsers.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredUsers = users.filter {
        it.role != "Admin" && // Don't show admins
        (it.firstName.contains(searchQuery, ignoreCase = true) ||
         it.lastName.contains(searchQuery, ignoreCase = true) ||
         it.username.contains(searchQuery, ignoreCase = true))
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "User Management",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredUsers.size} users",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search users") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ElectricBlue) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = ElectricBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User List
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "👥",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No users found",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onToggleStatus = {
                                selectedUser = user
                                showDeactivateDialog = true
                            },
                            onDelete = {
                                selectedUser = user
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Deactivate/Activate Dialog
    if (showDeactivateDialog && selectedUser != null) {
        val isActive = selectedUser!!.isActive
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = {
                Text(
                    text = if (isActive) "Deactivate User?" else "Activate User?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isActive) {
                        "This user will no longer be able to access the system."
                    } else {
                        "This user will be able to access the system again."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            adminViewModel.toggleUserStatus(selectedUser!!.id)
                            showDeactivateDialog = false
                            selectedUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) ErrorRed else SuccessGreen
                    )
                ) {
                    Text(if (isActive) "Deactivate" else "Activate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CardBackground
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete User?",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete ${selectedUser!!.firstName} ${selectedUser!!.lastName}?",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ This will permanently delete:",
                        color = DangerRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• User account\n• All reports filed by this user\n• All related data",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This action cannot be undone!",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            adminViewModel.deleteUser(selectedUser!!.id)
                            showDeleteDialog = false
                            selectedUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun UserCard(
    user: User,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "@${user.username}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (user.role) {
                            "Officer" -> InfoBlue.copy(alpha = 0.2f)
                            else -> ElectricBlue.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = user.role,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (user.role) {
                                "Officer" -> InfoBlue
                                else -> ElectricBlue
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (user.isActive) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (user.isActive) "Active" else "Inactive",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (user.isActive) SuccessGreen else ErrorRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Toggle Status Button
            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                    contentDescription = if (user.isActive) "Deactivate" else "Activate",
                    tint = if (user.isActive) ErrorRed else SuccessGreen
                )
            }
            
            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete User",
                    tint = DangerRed
                )
            }
        }
    }
}
