package com.example.blottermanagementsystem.ui.screens.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.ui.components.EmptyState
import com.example.blottermanagementsystem.ui.theme.*
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReport: (Int) -> Unit = {},
    notificationViewModel: NotificationViewModel = viewModel()
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val userId = preferencesManager.userId
    
    // Get notifications from database
    val notifications by notificationViewModel.getNotificationsByUser(userId).collectAsState(initial = emptyList())
    var selectedNotifications by remember { mutableStateOf(setOf<Int>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    
    // Function to mark notification as read
    fun markAsRead(notificationId: Int) {
        notificationViewModel.markAsRead(notificationId)
    }
    
    // Function to delete notification
    fun deleteNotification(notificationId: Int) {
        notificationViewModel.deleteNotification(notificationId)
    }
    
    // Function to delete selected notifications
    fun deleteSelected() {
        selectedNotifications.forEach { notificationViewModel.deleteNotification(it) }
        selectedNotifications = emptySet()
        isSelectionMode = false
    }
    
    // Function to mark all as read
    fun markAllAsRead() {
        notificationViewModel.markAllAsRead(userId)
    }
    
    // Function to toggle selection
    fun toggleSelection(notificationId: Int) {
        selectedNotifications = if (notificationId in selectedNotifications) {
            selectedNotifications - notificationId
        } else {
            selectedNotifications + notificationId
        }
        if (selectedNotifications.isEmpty()) {
            isSelectionMode = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isSelectionMode) "${selectedNotifications.size} selected" else "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!isSelectionMode) {
                            Text(
                                text = "${notifications.count { !it.isRead }} unread",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = { deleteSelected() },
                            enabled = selectedNotifications.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = ErrorRed
                            )
                        }
                    } else if (notifications.any { !it.isRead }) {
                        IconButton(onClick = { markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Mark all as read",
                                tint = ElectricBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            EmptyState(
                icon = "🔔",
                title = "No Notifications",
                message = "You're all caught up! No new notifications at the moment.",
                actionText = null,
                onActionClick = null
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = true
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        isSelected = notification.id in selectedNotifications,
                        isSelectionMode = isSelectionMode,
                        onMarkAsRead = { markAsRead(notification.id) },
                        onDelete = { deleteNotification(notification.id) },
                        onLongPress = {
                            isSelectionMode = true
                            toggleSelection(notification.id)
                        },
                        onToggleSelection = { toggleSelection(notification.id) },
                        onNavigateToReport = onNavigateToReport
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCard(
    notification: Notification,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelection: () -> Unit,
    onNavigateToReport: (Int) -> Unit
) {
    var showDeleteIcon by remember { mutableStateOf(false) }
    
    val iconColor = when (notification.type) {
        "CASE_ASSIGNED", "OFFICER_ASSIGNED" -> ElectricBlue
        "STATUS_UPDATE", "NEW_REPORT" -> InfoBlue
        "HEARING_SCHEDULED" -> WarningOrange
        "CASE_RESOLVED" -> SuccessGreen
        else -> TextSecondary
    }
    
    val icon = when (notification.type) {
        "CASE_ASSIGNED", "OFFICER_ASSIGNED" -> Icons.Default.Person
        "STATUS_UPDATE" -> Icons.Default.Update
        "HEARING_SCHEDULED" -> Icons.Default.DateRange
        "CASE_RESOLVED" -> Icons.Default.CheckCircle
        "NEW_REPORT" -> Icons.Default.Add
        else -> Icons.Default.Notifications
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> ElectricBlue.copy(alpha = 0.2f)
                notification.isRead -> CardBackground
                else -> CardBackground.copy(alpha = 1.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelection()
                        } else {
                            if (!notification.isRead) {
                                onMarkAsRead()
                            }
                            // Navigate to related report if available
                            notification.caseId?.let { caseId ->
                                onNavigateToReport(caseId)
                            }
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode) {
                            onLongPress()
                        } else {
                            showDeleteIcon = !showDeleteIcon
                        }
                    }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    when {
                        isSelectionMode -> {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleSelection() },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ElectricBlue,
                                    uncheckedColor = TextSecondary
                                )
                            )
                        }
                        showDeleteIcon -> {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        !notification.isRead -> {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                        .format(Date(notification.timestamp)),
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
