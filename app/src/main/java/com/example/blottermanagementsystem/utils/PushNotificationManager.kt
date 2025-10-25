package com.example.blottermanagementsystem.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blottermanagementsystem.MainActivity
import com.example.blottermanagementsystem.R

/**
 * Manager for Android Push Notifications
 * Handles creating notification channels and showing push notifications
 */
class PushNotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        // Notification Channels
        const val CHANNEL_REPORTS = "reports_channel"
        const val CHANNEL_HEARINGS = "hearings_channel"
        const val CHANNEL_ASSIGNMENTS = "assignments_channel"
        const val CHANNEL_UPDATES = "updates_channel"
        const val CHANNEL_GENERAL = "general_channel"
        
        // Notification IDs
        private var notificationId = 1000
        
        fun getNextNotificationId(): Int = notificationId++
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reports Channel
            val reportsChannel = NotificationChannel(
                CHANNEL_REPORTS,
                "Reports",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new reports and report updates"
                enableLights(true)
                enableVibration(true)
            }
            
            // Hearings Channel
            val hearingsChannel = NotificationChannel(
                CHANNEL_HEARINGS,
                "Hearings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled hearings"
                enableLights(true)
                enableVibration(true)
            }
            
            // Assignments Channel
            val assignmentsChannel = NotificationChannel(
                CHANNEL_ASSIGNMENTS,
                "Case Assignments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for case assignments"
                enableLights(true)
                enableVibration(true)
            }
            
            // Updates Channel
            val updatesChannel = NotificationChannel(
                CHANNEL_UPDATES,
                "Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for case status updates"
                enableLights(true)
                enableVibration(false)
            }
            
            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications"
                enableLights(true)
                enableVibration(false)
            }
            
            // Register channels
            notificationManager.createNotificationChannel(reportsChannel)
            notificationManager.createNotificationChannel(hearingsChannel)
            notificationManager.createNotificationChannel(assignmentsChannel)
            notificationManager.createNotificationChannel(updatesChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
    
    /**
     * Show a push notification
     */
    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_GENERAL,
        reportId: Int? = null,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        // Check if notifications are enabled
        val preferencesManager = PreferencesManager(context)
        if (!preferencesManager.notificationsEnabled) {
            return
        }
        
        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            reportId?.let {
                putExtra("REPORT_ID", it)
                putExtra("NAVIGATE_TO_REPORT", true)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            getNextNotificationId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Add sound if enabled
        if (preferencesManager.notificationSoundEnabled) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(defaultSoundUri)
        }
        
        // Add vibration for high priority
        if (priority == NotificationCompat.PRIORITY_HIGH) {
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
        }
        
        // Show notification
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(getNextNotificationId(), builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
            e.printStackTrace()
        }
    }
    
    /**
     * Show notification for new report
     */
    fun notifyNewReport(caseNumber: String, reportedBy: String, reportId: Int) {
        showNotification(
            title = "New Report Filed",
            message = "$reportedBy filed a new report: $caseNumber",
            channelId = CHANNEL_REPORTS,
            reportId = reportId,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    /**
     * Show notification for status change
     */
    fun notifyStatusChange(caseNumber: String, newStatus: String, reportId: Int) {
        showNotification(
            title = "Report Status Updated",
            message = "Case $caseNumber status changed to $newStatus",
            channelId = CHANNEL_UPDATES,
            reportId = reportId,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }
    
    /**
     * Show notification for case assignment
     */
    fun notifyCaseAssignment(caseNumber: String, reportId: Int) {
        showNotification(
            title = "New Case Assigned",
            message = "You have been assigned to case $caseNumber",
            channelId = CHANNEL_ASSIGNMENTS,
            reportId = reportId,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    /**
     * Show notification for hearing scheduled
     */
    fun notifyHearingScheduled(caseNumber: String, hearingDate: String, reportId: Int) {
        showNotification(
            title = "Hearing Scheduled",
            message = "A hearing for case $caseNumber has been scheduled on $hearingDate",
            channelId = CHANNEL_HEARINGS,
            reportId = reportId,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    /**
     * Show notification for case resolved
     */
    fun notifyCaseResolved(caseNumber: String, resolutionType: String, reportId: Int) {
        showNotification(
            title = "Case Resolved",
            message = "Case $caseNumber has been resolved: $resolutionType",
            channelId = CHANNEL_REPORTS,
            reportId = reportId,
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    /**
     * Show notification for evidence added
     */
    fun notifyEvidenceAdded(caseNumber: String, evidenceType: String, reportId: Int) {
        showNotification(
            title = "Evidence Added",
            message = "New evidence ($evidenceType) added to case $caseNumber",
            channelId = CHANNEL_UPDATES,
            reportId = reportId,
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Clear specific notification
     */
    fun clearNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
