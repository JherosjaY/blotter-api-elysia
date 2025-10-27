package com.example.blottermanagementsystem.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.blottermanagementsystem.MainActivity
import com.example.blottermanagementsystem.R
import com.example.blottermanagementsystem.data.api.ApiConfig
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.data.repository.ApiRepository
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications from Firebase
 */
class BlotterFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID_REPORTS = "blotter_reports"
        private const val CHANNEL_ID_STATUS = "status_updates"
        private const val CHANNEL_ID_HEARINGS = "hearings"
        private const val CHANNEL_ID_GENERAL = "general"
    }

    /**
     * Called when a new FCM token is generated
     * This happens on app install, reinstall, or token refresh
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔥 New FCM Token: $token")
        
        // Save token to preferences
        val preferencesManager = PreferencesManager(this)
        preferencesManager.fcmToken = token
        
        // TODO: Send token to backend server
        // This allows the server to send notifications to this device
        sendTokenToServer(token)
    }

    /**
     * Called when a message is received from Firebase
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "🔥 FCM Message received from: ${message.from}")
        Log.d(TAG, "🔥 Message data: ${message.data}")
        
        // Save notification to local database and get ID
        val notificationId = saveNotificationToDatabase(message)
        
        // Check if message contains a notification payload
        message.notification?.let { notification ->
            Log.d(TAG, "🔥 Notification Title: ${notification.title}")
            Log.d(TAG, "🔥 Notification Body: ${notification.body}")
            
            showNotification(
                title = notification.title ?: "Blotter Update",
                body = notification.body ?: "",
                data = message.data,
                notificationId = notificationId
            )
        }
        
        // Check if message contains data payload
        if (message.data.isNotEmpty()) {
            handleDataPayload(message.data, notificationId)
        }
    }

    /**
     * Save notification to local database for notification bell
     * Returns the notification ID
     */
    private fun saveNotificationToDatabase(message: RemoteMessage): Long {
        var notificationId = -1L
        try {
            val preferencesManager = PreferencesManager(this@BlotterFirebaseMessagingService)
            val userId = preferencesManager.userId
            
            if (userId == -1) {
                Log.w(TAG, "⚠️ User not logged in, skipping notification save")
                return notificationId
            }
            
            val database = BlotterDatabase.getDatabase(this@BlotterFirebaseMessagingService)
            val notificationDao = database.notificationDao()
            
            val title = message.notification?.title ?: message.data["title"] ?: "Blotter Update"
            val body = message.notification?.body ?: message.data["body"] ?: ""
            val type = message.data["type"] ?: "general"
            val caseId = message.data["report_id"]?.toIntOrNull() ?: message.data["case_id"]?.toIntOrNull()
            
            val notification = Notification(
                userId = userId,
                title = title,
                message = body,
                type = type,
                caseId = caseId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            
            // Insert and get ID synchronously
            notificationId = kotlinx.coroutines.runBlocking {
                notificationDao.insertNotification(notification)
            }
            Log.d(TAG, "✅ Notification saved to local database with ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving notification to database: ${e.message}", e)
        }
        return notificationId
    }

    /**
     * Handle data payload from FCM
     */
    private fun handleDataPayload(data: Map<String, String>, notificationId: Long) {
        val type = data["type"] ?: return
        
        when (type) {
            "new_report" -> {
                val caseNumber = data["case_number"] ?: ""
                val reportId = data["report_id"] ?: ""
                showNotification(
                    title = "New Report Filed",
                    body = "Case #$caseNumber has been filed",
                    channelId = CHANNEL_ID_REPORTS,
                    data = data,
                    notificationId = notificationId
                )
            }
            
            "status_update" -> {
                val caseNumber = data["case_number"] ?: ""
                val newStatus = data["new_status"] ?: ""
                showNotification(
                    title = "Status Update",
                    body = "Case #$caseNumber is now $newStatus",
                    channelId = CHANNEL_ID_STATUS,
                    data = data,
                    notificationId = notificationId
                )
            }
            
            "case_assigned" -> {
                val caseNumber = data["case_number"] ?: ""
                showNotification(
                    title = "New Case Assigned",
                    body = "Case #$caseNumber has been assigned to you",
                    channelId = CHANNEL_ID_REPORTS,
                    data = data,
                    notificationId = notificationId
                )
            }
            
            "hearing_scheduled" -> {
                val caseNumber = data["case_number"] ?: ""
                val hearingDate = data["hearing_date"] ?: ""
                showNotification(
                    title = "Hearing Scheduled",
                    body = "Hearing for Case #$caseNumber on $hearingDate",
                    channelId = CHANNEL_ID_HEARINGS,
                    data = data,
                    notificationId = notificationId
                )
            }
            
            "hearing_reminder" -> {
                val caseNumber = data["case_number"] ?: ""
                showNotification(
                    title = "Hearing Reminder",
                    body = "Hearing for Case #$caseNumber is tomorrow",
                    channelId = CHANNEL_ID_HEARINGS,
                    data = data,
                    notificationId = notificationId
                )
            }
            
            else -> {
                Log.w(TAG, "Unknown notification type: $type")
            }
        }
    }

    /**
     * Show notification to user
     */
    private fun showNotification(
        title: String,
        body: String,
        channelId: String = CHANNEL_ID_GENERAL,
        data: Map<String, String> = emptyMap(),
        notificationId: Long = -1
    ) {
        createNotificationChannels()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Add navigation data
            val notificationType = data["type"] ?: ""
            val caseId = data["case_id"]?.toIntOrNull() ?: data["report_id"]?.toIntOrNull()
            
            // Add notification ID for marking as read
            if (notificationId != -1L) {
                putExtra("notification_id", notificationId.toInt())
            }
            
            // Set navigation destination based on notification type
            when (notificationType) {
                "new_report", "status_update", "case_assigned", "hearing_scheduled", "hearing_reminder" -> {
                    // Navigate to specific case
                    if (caseId != null) {
                        putExtra("navigate_to", "case_detail")
                        putExtra("case_id", caseId)
                    }
                }
                else -> {
                    // Navigate to notifications screen
                    putExtra("navigate_to", "notifications")
                }
            }
            
            // Add all data to intent
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_bms_notification) // B.M.S. app icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // Expandable text
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Create notification channels for Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_REPORTS,
                    "Blotter Reports",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new blotter reports"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_ID_STATUS,
                    "Status Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for case status updates"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_ID_HEARINGS,
                    "Hearings",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for scheduled hearings"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "General app notifications"
                }
            )
            
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    /**
     * Send FCM token to backend server
     */
    private fun sendTokenToServer(token: String) {
        val preferencesManager = PreferencesManager(this)
        val userId = preferencesManager.userId
        
        // Only send if user is logged in
        if (userId == -1) {
            Log.d(TAG, "⚠️ User not logged in, skipping token send")
            return
        }
        
        // Send token to backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ApiRepository()
                val result = repository.updateFcmToken(userId, token)
                
                if (result.isSuccess) {
                    Log.d(TAG, "✅ FCM token sent to backend successfully")
                } else {
                    Log.e(TAG, "❌ Failed to send FCM token to backend")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending FCM token: ${e.message}", e)
            }
        }
    }
}
