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
import com.example.blottermanagementsystem.data.entity.ActivityLog
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val activityLogs by viewModel.getRecentActivityLogs().collectAsState(initial = emptyList())
    var filterType by remember { mutableStateOf("All") }
    val filterTypes = listOf("All", "Report Filed", "Status Change", "User Action", "Officer Action")
    
    val filteredLogs = activityLogs
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Activity Logs",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredLogs.size} activities",
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
            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filterTypes.take(3).forEach { type ->
                    FilterChip(
                        selected = filterType == type,
                        onClick = { filterType = type },
                        label = { Text(type, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = TextPrimary,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity List
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📋",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No activity logs",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        ActivityLogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLogCard(log: ActivityLog) {
    val actionType: String = log.activityType
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon based on action type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            actionType.contains("Filed", ignoreCase = true) -> SuccessGreen.copy(alpha = 0.2f)
                            actionType.contains("Status", ignoreCase = true) -> InfoBlue.copy(alpha = 0.2f)
                            actionType.contains("Delete", ignoreCase = true) -> ErrorRed.copy(alpha = 0.2f)
                            actionType.contains("Update", ignoreCase = true) -> WarningOrange.copy(alpha = 0.2f)
                            else -> ElectricBlue.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        actionType.contains("Filed", ignoreCase = true) -> Icons.Default.Add
                        actionType.contains("Status", ignoreCase = true) -> Icons.Default.Update
                        actionType.contains("Delete", ignoreCase = true) -> Icons.Default.Delete
                        actionType.contains("Update", ignoreCase = true) -> Icons.Default.Edit
                        actionType.contains("Login", ignoreCase = true) -> Icons.Default.Login
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when {
                        actionType.contains("Filed", ignoreCase = true) -> SuccessGreen
                        actionType.contains("Status", ignoreCase = true) -> InfoBlue
                        actionType.contains("Delete", ignoreCase = true) -> ErrorRed
                        actionType.contains("Update", ignoreCase = true) -> WarningOrange
                        else -> ElectricBlue
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Activity Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = actionType,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // User
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.performedBy,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                                .format(Date(log.timestamp)),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
