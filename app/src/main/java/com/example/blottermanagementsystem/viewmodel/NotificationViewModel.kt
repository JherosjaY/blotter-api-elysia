package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BlotterDatabase.getDatabase(application)
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
        mediationSessionDao = database.mediationSessionDao()
    )
    
    // Get notifications for a specific user
    fun getNotificationsByUser(userId: Int): Flow<List<Notification>> {
        return repository.getNotificationsByUserId(userId)
    }
    
    // Get unread count
    fun getUnreadCount(userId: Int): Flow<Int> {
        return repository.getUnreadNotificationCount(userId)
    }
    
    // Mark notification as read
    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }
    
    // Mark all as read for a user
    fun markAllAsRead(userId: Int) {
        viewModelScope.launch {
            repository.markAllAsRead(userId)
        }
    }
    
    // Delete notification
    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            repository.deleteNotificationById(notificationId)
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
}
