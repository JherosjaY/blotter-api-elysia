package com.example.blottermanagementsystem.ui.screens.sms

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.SmsNotification
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.viewmodel.SmsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsNotificationListScreen(
    reportId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: SmsViewModel = viewModel()
) {
    val notifications by if (reportId != null) {
        viewModel.getNotificationsByReportId(reportId).collectAsState(initial = emptyList())
    } else {
        viewModel.getAllNotifications().collectAsState(initial = emptyList())
    }
    
    var filterStatus by remember { mutableStateOf("All") }
    val statusOptions = listOf("All", "Pending", "Sent", "Delivered", "Failed", "Replied")
    
    val filteredNotifications = if (filterStatus == "All") {
        notifications
    } else {
        notifications.filter { it.deliveryStatus == filterStatus }
    }
    
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
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        SmsNotificationCard(notification)
                    }
                }
            }
        }
    }
}

@Composable
fun SmsNotificationCard(notification: SmsNotification) {
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
                    verticalAlignment = Alignment.CenterVertically
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
                        notification.messageType.replace("_", " "),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message Content
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    notification.messageContent,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
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
                        color = Color.Gray
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
