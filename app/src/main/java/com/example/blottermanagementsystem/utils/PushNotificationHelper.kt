package com.example.blottermanagementsystem.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blottermanagementsystem.MainActivity
import com.example.blottermanagementsystem.R
import com.example.blottermanagementsystem.data.entity.BlotterReport

/**
 * Push Notification Helper
 * Handles local push notifications for the app
 */
object PushNotificationHelper {
    
    private const val CHANNEL_ID_REPORTS = "blotter_reports"
    private const val CHANNEL_ID_STATUS = "status_updates"
    private const val CHANNEL_ID_HEARINGS = "hearings"
    private const val CHANNEL_ID_GENERAL = "general"
    
    private const val CHANNEL_NAME_REPORTS = "Blotter Reports"
    private const val CHANNEL_NAME_STATUS = "Status Updates"
    private const val CHANNEL_NAME_HEARINGS = "Hearings"
    private const val CHANNEL_NAME_GENERAL = "General Notifications"
    
    /**
     * Create notification channels (Android 8.0+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Reports Channel
            val reportsChannel = NotificationChannel(
                CHANNEL_ID_REPORTS,
                CHANNEL_NAME_REPORTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new blotter reports"
                enableVibration(true)
                enableLights(true)
            }
            
            // Status Updates Channel
            val statusChannel = NotificationChannel(
                CHANNEL_ID_STATUS,
                CHANNEL_NAME_STATUS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for case status updates"
                enableVibration(true)
                enableLights(true)
            }
            
            // Hearings Channel
            val hearingsChannel = NotificationChannel(
                CHANNEL_ID_HEARINGS,
                CHANNEL_NAME_HEARINGS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled hearings"
                enableVibration(true)
                enableLights(true)
            }
            
            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                CHANNEL_NAME_GENERAL,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            
            notificationManager.createNotificationChannel(reportsChannel)
            notificationManager.createNotificationChannel(statusChannel)
            notificationManager.createNotificationChannel(hearingsChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
    
    /**
     * Send notification for new report filed
     */
    fun sendReportFiledNotification(
        context: Context,
        report: BlotterReport
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("report_id", report.id)
            putExtra("navigate_to", "report_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REPORTS)
            .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists
            .setContentTitle("Report Filed Successfully")
            .setContentText("Case #${report.caseNumber} - ${report.incidentType}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your blotter report has been filed successfully.\n\nCase Number: ${report.caseNumber}\nIncident: ${report.incidentType}\nStatus: ${report.status}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(report.id, notification)
    }
    
    /**
     * Send notification for status update
     */
    fun sendStatusUpdateNotification(
        context: Context,
        report: BlotterReport,
        oldStatus: String,
        newStatus: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("report_id", report.id)
            putExtra("navigate_to", "report_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Status Updated")
            .setContentText("Case #${report.caseNumber}: $oldStatus → $newStatus")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your case status has been updated.\n\nCase Number: ${report.caseNumber}\nPrevious Status: $oldStatus\nNew Status: $newStatus"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(report.id + 1000, notification)
    }
    
    /**
     * Send notification for case assigned to officer
     */
    fun sendCaseAssignedNotification(
        context: Context,
        report: BlotterReport,
        officerName: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("report_id", report.id)
            putExtra("navigate_to", "report_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id + 2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REPORTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Case Assigned")
            .setContentText("Case #${report.caseNumber} assigned to Officer $officerName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your case has been assigned to an officer.\n\nCase Number: ${report.caseNumber}\nOfficer: $officerName\nIncident: ${report.incidentType}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(report.id + 2000, notification)
    }
    
    /**
     * Send notification for scheduled hearing
     */
    fun sendHearingScheduledNotification(
        context: Context,
        report: BlotterReport,
        hearingDate: String,
        hearingTime: String,
        location: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("report_id", report.id)
            putExtra("navigate_to", "report_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id + 3000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_HEARINGS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hearing Scheduled")
            .setContentText("Case #${report.caseNumber} - $hearingDate at $hearingTime")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("A hearing has been scheduled for your case.\n\nCase Number: ${report.caseNumber}\nDate: $hearingDate\nTime: $hearingTime\nLocation: $location"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(report.id + 3000, notification)
    }
    
    /**
     * Send hearing reminder notification (1 day before)
     */
    fun sendHearingReminderNotification(
        context: Context,
        report: BlotterReport,
        hearingDate: String,
        hearingTime: String,
        location: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("report_id", report.id)
            putExtra("navigate_to", "report_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id + 4000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_HEARINGS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hearing Reminder")
            .setContentText("Your hearing is tomorrow - Case #${report.caseNumber}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Reminder: Your hearing is scheduled for tomorrow.\n\nCase Number: ${report.caseNumber}\nDate: $hearingDate\nTime: $hearingTime\nLocation: $location\n\nPlease bring valid ID and all relevant documents."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(report.id + 4000, notification)
    }
    
    /**
     * Send case resolved notification
     */
    fun sendCaseResolvedNotification(
        context: Context,
        report: BlotterReport,
        resolution: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("report_id", report.id)
            putExtra("navigate_to", "report_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            report.id + 5000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Case Resolved")
            .setContentText("Case #${report.caseNumber} has been resolved")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your case has been resolved.\n\nCase Number: ${report.caseNumber}\nResolution: $resolution\n\nThank you for your cooperation."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(report.id + 5000, notification)
    }
    
    /**
     * Send general notification
     */
    fun sendGeneralNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    /**
     * Cancel notification by ID
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
