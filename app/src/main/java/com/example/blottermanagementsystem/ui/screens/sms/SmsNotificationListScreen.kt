package com.example.blottermanagementsystem.ui.screens.sms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.SmsNotification
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.LazyListOptimizer
import com.example.blottermanagementsystem.utils.rememberPaginationState
import com.example.blottermanagementsystem.viewmodel.DashboardViewModel
import com.example.blottermanagementsystem.viewmodel.SmsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsNotificationListScreen(
    reportId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: SmsViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val userRole = preferencesManager.userRole ?: "User"
    val userId = preferencesManager.userId
    
    // Get all reports for filtering
    val allReports by dashboardViewModel.allReports.collectAsState(initial = emptyList())
    
    val notifications by if (reportId != null) {
        viewModel.getNotificationsByReportId(reportId).collectAsState(initial = emptyList())
    } else {
        viewModel.getAllNotifications().collectAsState(initial = emptyList())
    }
    
    // Filter notifications based on role
    val roleFilteredNotifications = when (userRole) {
        "Officer" -> {
            // Officers only see SMS for their assigned cases
            val assignedReportIds = allReports
                .filter { report ->
                    val assignedOfficerIds = report.assignedOfficerIds
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                    assignedOfficerIds.contains(userId)
                }
                .map { it.id }
            
            notifications.filter { notification ->
                assignedReportIds.contains(notification.blotterReportId)
            }
        }
        "Admin" -> {
            // Admins see all SMS
            notifications
        }
        else -> {
            // Users see SMS for their own reports
            val userReportIds = allReports
                .filter { it.userId == userId }
                .map { it.id }
            
            notifications.filter { notification ->
                userReportIds.contains(notification.blotterReportId)
            }
        }
    }
    
    var filterStatus by remember { mutableStateOf("All") }
    val statusOptions = listOf("All", "Pending", "Sent", "Delivered")
    
    val filteredNotifications = remember(roleFilteredNotifications, filterStatus) {
        if (filterStatus == "All") {
            roleFilteredNotifications
        } else {
            roleFilteredNotifications.filter { it.deliveryStatus == filterStatus }
        }
    }
    val paginationState = rememberPaginationState(filteredNotifications, pageSize = 20)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("SMS Notifications", fontWeight = FontWeight.Bold)
                        if (reportId != null) {
                            Text(
                                "Case #$reportId",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips - Scrollable
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusOptions.size) { index ->
                    val status = statusOptions[index]
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status, fontSize = 13.sp) },
                        leadingIcon = {
                            if (filterStatus == status) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White,
                            containerColor = CardBackground,
                            labelColor = Color.Gray
                        )
                    )
                }
            }
            
            // Notification List
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Sms,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No SMS notifications",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                val listState = LazyListOptimizer.rememberOptimizedLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(LazyListOptimizer.OPTIMAL_CONTENT_PADDING),
                    verticalArrangement = Arrangement.spacedBy(LazyListOptimizer.OPTIMAL_ITEM_SPACING)
                ) {
                    items(paginationState.visibleItems, key = { it.id }) { notification ->
                        SmsNotificationCard(
                            notification = notification,
                            onResend = {
                                viewModel.resendSms(notification.id)
                            },
                            onDelete = {
                                viewModel.deleteSms(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmsNotificationCard(
    notification: SmsNotification,
    onResend: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        when (notification.messageType) {
                            "INITIAL_NOTICE" -> Icons.Default.NotificationImportant
                            "REMINDER" -> Icons.Default.Alarm
                            "HEARING_NOTICE" -> Icons.Default.Event
                            "FOLLOW_UP" -> Icons.Default.Update
                            else -> Icons.Default.Sms
                        },
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (notification.messageType) {
                            "INITIAL_NOTICE" -> "Initial Notice"
                            "HEARING_NOTICE" -> "Hearing Notice"
                            "FOLLOW_UP" -> "Follow Up"
                            "REMINDER" -> "Reminder"
                            else -> notification.messageType.replace("_", " ")
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                StatusBadge(notification.deliveryStatus)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recipient
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    notification.recipientNumber,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message Content
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    notification.messageContent,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        formatDate(notification.sentDate),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (notification.deliveryStatus == "Delivered") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatDate(notification.sentDate),
                            fontSize = 12.sp,
                            color = SuccessGreen
                        )
                    }
                }
            }
            
            // Reply Section
            notification.respondentReply?.let { reply ->
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Reply,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Respondent Reply:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            reply,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        
                        notification.replyDate?.let { replyDate ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatDate(replyDate),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Action Buttons (for Pending or Failed)
            if (notification.deliveryStatus == "Pending" || notification.deliveryStatus == "Failed") {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DividerColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Resend Button
                    OutlinedButton(
                        onClick = onResend,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (notification.deliveryStatus == "Failed") WarningOrange else ElectricBlue
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (notification.deliveryStatus == "Failed") WarningOrange else ElectricBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (notification.deliveryStatus == "Failed") "Retry" else "Resend",
                            fontSize = 13.sp
                        )
                    }
                    
                    // Delete Button
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DangerRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, DangerRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, icon) = when (status) {
        "Sent" -> Pair(ElectricBlue, Icons.Default.Send)
        "Delivered" -> Pair(SuccessGreen, Icons.Default.DoneAll)
        "Failed" -> Pair(DangerRed, Icons.Default.Error)
        "Replied" -> Pair(SuccessGreen, Icons.Default.Reply)
        else -> Pair(WarningYellow, Icons.Default.Schedule)
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                status,
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
