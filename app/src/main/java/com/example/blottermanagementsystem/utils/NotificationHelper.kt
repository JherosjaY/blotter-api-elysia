package com.example.blottermanagementsystem.utils

import android.content.Context
import com.example.blottermanagementsystem.data.entity.ActivityLog
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.data.repository.BlotterRepository

/**
 * Helper class to trigger notifications and activity logs together
 * This ensures realtime updates across the app
 */
class NotificationHelper(
    private val repository: BlotterRepository,
    private val context: Context? = null
) {
    private val pushNotificationManager: PushNotificationManager? = 
        context?.let { PushNotificationManager(it) }
    
    /**
     * Notify when a new report is filed
     */
    suspend fun notifyNewReport(
        adminUserId: Int,
        caseNumber: String,
        reportedBy: String,
        reportId: Int,
        performedBy: String
    ) {
        // Create notification for admin
        val notification = Notification(
            userId = adminUserId,
            title = "New Report Filed",
            message = "$reportedBy filed a new report: $caseNumber",
            type = "NEW_REPORT",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(notification)
        
        // Show push notification
        pushNotificationManager?.notifyNewReport(caseNumber, reportedBy, reportId)
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Report Filed",
            description = "New report filed by $reportedBy",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when report status changes
     */
    suspend fun notifyStatusChange(
        userId: Int,
        caseNumber: String,
        oldStatus: String,
        newStatus: String,
        reportId: Int,
        performedBy: String
    ) {
        // Create notification
        val notification = Notification(
            userId = userId,
            title = "Report Status Updated",
            message = "Case $caseNumber status changed from $oldStatus to $newStatus",
            type = "STATUS_UPDATE",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(notification)
        
        // Show push notification
        pushNotificationManager?.notifyStatusChange(caseNumber, newStatus, reportId)
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Status Change",
            description = "Status changed from $oldStatus to $newStatus",
            oldValue = oldStatus,
            newValue = newStatus,
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when officer is assigned to a case
     */
    suspend fun notifyOfficerAssignment(
        officerUserId: Int,
        adminUserId: Int,
        caseNumber: String,
        officerName: String,
        reportId: Int,
        performedBy: String
    ) {
        // Notify the officer
        val officerNotification = Notification(
            userId = officerUserId,
            title = "New Case Assigned",
            message = "You have been assigned to case $caseNumber",
            type = "CASE_ASSIGNED",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(officerNotification)
        
        // Show push notification to officer
        pushNotificationManager?.notifyCaseAssignment(caseNumber, reportId)
        
        // Notify admin
        val adminNotification = Notification(
            userId = adminUserId,
            title = "Officer Assigned",
            message = "Officer $officerName has been assigned to case $caseNumber",
            type = "OFFICER_ASSIGNED",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(adminNotification)
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Officer Assigned",
            description = "Officer $officerName assigned to case",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when hearing is scheduled
     */
    suspend fun notifyHearingScheduled(
        userIds: List<Int>, // Can notify multiple users (complainant, respondent, officer)
        caseNumber: String,
        hearingDate: String,
        reportId: Int,
        performedBy: String
    ) {
        // Create notifications for all relevant users
        userIds.forEach { userId ->
            val notification = Notification(
                userId = userId,
                title = "Hearing Scheduled",
                message = "A hearing for case $caseNumber has been scheduled on $hearingDate",
                type = "HEARING_SCHEDULED",
                caseId = reportId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertNotification(notification)
            
            // Show push notification
            pushNotificationManager?.notifyHearingScheduled(caseNumber, hearingDate, reportId)
        }
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Hearing Scheduled",
            description = "Hearing scheduled for $hearingDate",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when case is resolved
     */
    suspend fun notifyCaseResolved(
        userIds: List<Int>, // Notify complainant, officer, admin
        caseNumber: String,
        resolutionType: String,
        reportId: Int,
        performedBy: String
    ) {
        // Create notifications for all relevant users
        userIds.forEach { userId ->
            val notification = Notification(
                userId = userId,
                title = "Case Resolved",
                message = "Case $caseNumber has been resolved: $resolutionType",
                type = "CASE_RESOLVED",
                caseId = reportId,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertNotification(notification)
            
            // Show push notification
            pushNotificationManager?.notifyCaseResolved(caseNumber, resolutionType, reportId)
        }
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Case Resolved",
            description = "Case resolved: $resolutionType",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when evidence is added
     */
    suspend fun notifyEvidenceAdded(
        officerUserId: Int,
        adminUserId: Int,
        caseNumber: String,
        evidenceType: String,
        reportId: Int,
        performedBy: String
    ) {
        // Notify admin
        val notification = Notification(
            userId = adminUserId,
            title = "Evidence Added",
            message = "New evidence ($evidenceType) added to case $caseNumber",
            type = "EVIDENCE_ADDED",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(notification)
        
        // Show push notification
        pushNotificationManager?.notifyEvidenceAdded(caseNumber, evidenceType, reportId)
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Evidence Added",
            description = "Evidence added: $evidenceType",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when witness is added
     */
    suspend fun notifyWitnessAdded(
        adminUserId: Int,
        caseNumber: String,
        witnessName: String,
        reportId: Int,
        performedBy: String
    ) {
        // Notify admin
        val notification = Notification(
            userId = adminUserId,
            title = "Witness Added",
            message = "Witness $witnessName added to case $caseNumber",
            type = "WITNESS_ADDED",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(notification)
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Witness Added",
            description = "Witness added: $witnessName",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
    
    /**
     * Notify when suspect is added
     */
    suspend fun notifySuspectAdded(
        adminUserId: Int,
        caseNumber: String,
        suspectName: String,
        reportId: Int,
        performedBy: String
    ) {
        // Notify admin
        val notification = Notification(
            userId = adminUserId,
            title = "Suspect Added",
            message = "Suspect $suspectName added to case $caseNumber",
            type = "SUSPECT_ADDED",
            caseId = reportId,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNotification(notification)
        
        // Log activity
        val activityLog = ActivityLog(
            caseId = reportId,
            caseTitle = caseNumber,
            activityType = "Suspect Added",
            description = "Suspect added: $suspectName",
            performedBy = performedBy,
            timestamp = System.currentTimeMillis()
        )
        repository.insertActivityLog(activityLog)
    }
}
