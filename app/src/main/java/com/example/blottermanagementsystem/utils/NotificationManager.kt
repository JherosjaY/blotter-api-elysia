package com.example.blottermanagementsystem.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.blottermanagementsystem.MainActivity
import com.example.blottermanagementsystem.R

class LocalNotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID = "blotter_notifications"
        const val CHANNEL_NAME = "Blotter Updates"
        const val CHANNEL_DESCRIPTION = "Notifications for case updates, hearings, and assignments"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun sendNotification(
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    fun sendCaseUpdateNotification(caseNumber: String, status: String) {
        sendNotification(
            title = "Case Update: $caseNumber",
            message = "Status changed to: $status"
        )
    }
    
    fun sendHearingReminderNotification(caseNumber: String, date: String, time: String) {
        sendNotification(
            title = "Hearing Reminder",
            message = "Case $caseNumber - $date at $time"
        )
    }
    
    fun sendAssignmentNotification(caseNumber: String) {
        sendNotification(
            title = "New Case Assignment",
            message = "You have been assigned to case: $caseNumber"
        )
    }
    
    fun sendResolutionNotification(caseNumber: String) {
        sendNotification(
            title = "Case Resolved",
            message = "Case $caseNumber has been resolved"
        )
    }
}
