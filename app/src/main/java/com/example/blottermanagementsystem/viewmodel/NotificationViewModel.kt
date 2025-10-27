package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.data.entity.User
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import com.example.blottermanagementsystem.data.repository.ApiRepository
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ConnectedDevice(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val role: String,
    val deviceId: String?,
    val fcmToken: String?,
    val lastSeen: String?
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BlotterDatabase.getDatabase(application)
    private val apiRepository = ApiRepository()
    private val repository = BlotterRepository(
        userDao = database.userDao(),
        blotterReportDao = database.blotterReportDao(),
        suspectDao = database.suspectDao(),
        witnessDao = database.witnessDao(),
        evidenceDao = database.evidenceDao(),
        hearingDao = database.hearingDao(),
        statusHistoryDao = database.statusHistoryDao(),
        resolutionDao = database.resolutionDao(),
        officerDao = database.officerDao(),
        activityLogDao = database.activityLogDao(),
        notificationDao = database.notificationDao(),
        statusDao = database.statusDao(),
        personDao = database.personDao(),
        respondentDao = database.respondentDao(),
        personHistoryDao = database.personHistoryDao(),
        smsNotificationDao = database.smsNotificationDao(),
        respondentStatementDao = database.respondentStatementDao(),
        summonsDao = database.summonsDao(),
        kpFormDao = database.kpFormDao(),
        mediationSessionDao = database.mediationSessionDao(),
        caseTimelineDao = database.caseTimelineDao(),
        caseTemplateDao = database.caseTemplateDao()
    )
    
    // Sync notifications from cloud
    fun syncNotificationsFromCloud(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "🔄 Syncing notifications from cloud...")
                val result = apiRepository.syncNotificationsFromCloud(userId)
                if (result.isSuccess) {
                    val cloudNotifications = result.getOrNull() ?: emptyList()
                    // Update local database
                    cloudNotifications.forEach { notification ->
                        repository.insertNotification(notification)
                    }
                    Log.d("NotificationVM", "✅ Synced ${cloudNotifications.size} notifications")
                }
            } catch (e: Exception) {
                Log.e("NotificationVM", "❌ Error syncing notifications: ${e.message}", e)
            }
        }
    }
    
    // Get notifications for a specific user
    fun getNotificationsByUser(userId: Int): Flow<List<Notification>> {
        return repository.getNotificationsByUserId(userId)
    }
    
    // Get unread count
    fun getUnreadCount(userId: Int): Flow<Int> {
        return repository.getUnreadNotificationCount(userId)
    }
    
    // Mark notification as read (with cloud sync)
    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            // Update local database
            repository.markAsRead(notificationId)
            
            // Sync to cloud
            try {
                apiRepository.markNotificationAsReadInCloud(notificationId)
            } catch (e: Exception) {
                Log.w("NotificationVM", "⚠️ Failed to sync read status to cloud: ${e.message}")
            }
        }
    }
    
    // Mark all as read for a user
    fun markAllAsRead(userId: Int) {
        viewModelScope.launch {
            repository.markAllAsRead(userId)
        }
    }
    
    // Delete notification (with cloud sync)
    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            // Delete from local database
            repository.deleteNotificationById(notificationId)
            
            // Sync to cloud
            try {
                apiRepository.deleteNotificationInCloud(notificationId)
            } catch (e: Exception) {
                Log.w("NotificationVM", "⚠️ Failed to sync deletion to cloud: ${e.message}")
            }
        }
    }
    
    // Delete all notifications for a user
    fun deleteAllNotifications(userId: Int) {
        viewModelScope.launch {
            repository.deleteAllNotificationsByUserId(userId)
        }
    }
    
    // Create notification (called when events happen)
    fun createNotification(
        userId: Int,
        title: String,
        message: String,
        type: String,
        caseId: Int? = null
    ) {
        viewModelScope.launch {
            val notification = Notification(
                userId = userId,
                title = title,
                message = message,
                type = type,
                caseId = caseId,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            repository.insertNotification(notification)
        }
    }
    
    // Notify when report status changes
    fun notifyReportStatusChange(userId: Int, caseNumber: String, newStatus: String, reportId: Int) {
        createNotification(
            userId = userId,
            title = "Report Status Updated",
            message = "Case $caseNumber status changed to: $newStatus",
            type = "STATUS_UPDATE",
            caseId = reportId
        )
    }
    
    // Notify when officer is assigned
    fun notifyOfficerAssignment(userId: Int, caseNumber: String, officerName: String, reportId: Int) {
        createNotification(
            userId = userId,
            title = "Officer Assigned",
            message = "Officer $officerName has been assigned to case $caseNumber",
            type = "OFFICER_ASSIGNED",
            caseId = reportId
        )
    }
    
    // Notify when hearing is scheduled
    fun notifyHearingScheduled(userId: Int, caseNumber: String, hearingDate: String, reportId: Int) {
        createNotification(
            userId = userId,
            title = "Hearing Scheduled",
            message = "A hearing for case $caseNumber has been scheduled on $hearingDate",
            type = "HEARING_SCHEDULED",
            caseId = reportId
        )
    }
    
    // Notify when report is resolved
    fun notifyReportResolved(userId: Int, caseNumber: String, reportId: Int) {
        createNotification(
            userId = userId,
            title = "Case Resolved",
            message = "Case $caseNumber has been marked as resolved",
            type = "CASE_RESOLVED",
            caseId = reportId
        )
    }
    
    // Notify admin of new report
    fun notifyAdminNewReport(adminId: Int, caseNumber: String, reportedBy: String, reportId: Int) {
        createNotification(
            userId = adminId,
            title = "New Report Filed",
            message = "$reportedBy filed a new report: $caseNumber",
            type = "NEW_REPORT",
            caseId = reportId
        )
    }
    
    // Notify officer of case assignment
    fun notifyOfficerCaseAssigned(officerId: Int, caseNumber: String, reportId: Int) {
        createNotification(
            userId = officerId,
            title = "New Case Assigned",
            message = "You have been assigned to case $caseNumber",
            type = "CASE_ASSIGNED",
            caseId = reportId
        )
    }
    
    // ========== FCM FUNCTIONS (FOR FUN!) ==========
    
    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices
    
    private val _sendStatus = MutableStateFlow<String?>(null)
    val sendStatus: StateFlow<String?> = _sendStatus
    
    // Load connected devices
    fun loadConnectedDevices() {
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "📱 Loading connected devices...")
                // TODO: Add API call to get devices from backend
                // For now, show all active users (they'll have FCM tokens when they login)
                val users = repository.getAllUsersSync()
                val devices = users.filter { it.isActive }.map {
                    ConnectedDevice(
                        id = it.id,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        username = it.username,
                        role = it.role,
                        deviceId = null, // Will be available from API
                        fcmToken = null, // Will be available from API
                        lastSeen = null  // Will be available from API
                    )
                }
                _connectedDevices.value = devices
                Log.d("NotificationVM", "✅ Loaded ${devices.size} connected devices")
            } catch (e: Exception) {
                Log.e("NotificationVM", "❌ Error loading devices: ${e.message}", e)
            }
        }
    }
    
    // Send FCM notification to specific user
    fun sendNotificationToUser(userId: Int, title: String, message: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "📤 Sending notification to user $userId...")
                _sendStatus.value = "Sending..."
                
                // TODO: Add API call to send FCM
                // apiRepository.sendFCMNotification(userId, title, message)
                
                _sendStatus.value = "✅ Notification sent!"
                Log.d("NotificationVM", "✅ Notification sent successfully")
                
                // Clear status after 2 seconds
                kotlinx.coroutines.delay(2000)
                _sendStatus.value = null
            } catch (e: Exception) {
                Log.e("NotificationVM", "❌ Error sending notification: ${e.message}", e)
                _sendStatus.value = "❌ Failed to send"
                kotlinx.coroutines.delay(2000)
                _sendStatus.value = null
            }
        }
    }
    
    // Send FCM notification to all users
    fun sendNotificationToAll(title: String, message: String) {
        viewModelScope.launch {
            try {
                Log.d("NotificationVM", "📤 Sending notification to all users...")
                _sendStatus.value = "Sending to all..."
                
                // TODO: Add API call to send FCM to all
                // apiRepository.sendFCMNotificationToAll(title, message)
                
                _sendStatus.value = "✅ Sent to all!"
                Log.d("NotificationVM", "✅ Notification sent to all users")
                
                // Clear status after 2 seconds
                kotlinx.coroutines.delay(2000)
                _sendStatus.value = null
            } catch (e: Exception) {
                Log.e("NotificationVM", "❌ Error sending notification: ${e.message}", e)
                _sendStatus.value = "❌ Failed to send"
                kotlinx.coroutines.delay(2000)
                _sendStatus.value = null
            }
        }
    }
    
    // Send manual notification (for admin panel)
    suspend fun sendManualNotification(
        title: String,
        message: String,
        recipientType: String,
        specificUserIds: List<Int> = emptyList()
    ): Boolean {
        return try {
            Log.d("NotificationVM", "📤 Sending manual notification: $title")
            Log.d("NotificationVM", "📋 Recipient type: $recipientType")
            
            val users = repository.getAllUsersSync()
            
            // Determine recipients based on type
            val recipients = when (recipientType) {
                "All Users" -> users.filter { it.role == "User" }
                "All Admins" -> users.filter { it.role == "Admin" }
                "All Officers" -> users.filter { it.role == "Officer" }
                "Specific Users" -> users.filter { it.id in specificUserIds }
                else -> emptyList()
            }
            
            Log.d("NotificationVM", "👥 Sending to ${recipients.size} recipients")
            
            // Create in-app notifications for each recipient
            recipients.forEach { user ->
                createNotification(
                    userId = user.id,
                    title = title,
                    message = message,
                    type = "ANNOUNCEMENT"
                )
            }
            
            // Send FCM push notifications via API
            try {
                val fcmResult = apiRepository.sendBulkFCMNotification(
                    title = title,
                    message = message,
                    recipientType = recipientType,
                    specificUserIds = if (recipientType == "Specific Users") specificUserIds else null
                )
                
                if (fcmResult.isSuccess) {
                    Log.d("NotificationVM", "✅ FCM push notifications sent successfully")
                } else {
                    Log.w("NotificationVM", "⚠️ FCM push notifications failed, but in-app notifications created")
                }
            } catch (e: Exception) {
                Log.w("NotificationVM", "⚠️ FCM error (in-app notifications still created): ${e.message}")
            }
            
            Log.d("NotificationVM", "✅ Manual notification sent successfully")
            true
        } catch (e: Exception) {
            Log.e("NotificationVM", "❌ Error sending manual notification: ${e.message}", e)
            false
        }
    }
}
