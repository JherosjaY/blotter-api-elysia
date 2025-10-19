package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.data.entity.ActivityLog
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalReports: Int = 0,
    val pendingReports: Int = 0,
    val ongoingReports: Int = 0,
    val resolvedReports: Int = 0,
    val archivedReports: Int = 0,
    val totalOfficers: Int = 0,
    val totalUsers: Int = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    
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
    
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats
    
    val allReports: Flow<List<BlotterReport>> = repository.getAllActiveReports()
    
    private val _recentReports = MutableStateFlow<List<BlotterReport>>(emptyList())
    val recentReports: StateFlow<List<BlotterReport>> = _recentReports
    
    init {
        loadDashboardStats()
        loadRecentReports()
    }
    
    private fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                val totalReports = repository.getActiveReportCount()
                val pendingReports = repository.getReportCountByStatus("Pending")
                val ongoingReports = repository.getReportCountByStatus("Under Investigation")
                val resolvedReports = repository.getReportCountByStatus("Resolved")
                val archivedReports = repository.getArchivedReportCount()
                val totalOfficers = repository.getOfficerCount()
                val totalUsers = repository.getTotalUserCount()
                
                _dashboardStats.value = DashboardStats(
                    totalReports = totalReports,
                    pendingReports = pendingReports,
                    ongoingReports = ongoingReports,
                    resolvedReports = resolvedReports,
                    archivedReports = archivedReports,
                    totalOfficers = totalOfficers,
                    totalUsers = totalUsers
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadRecentReports() {
        viewModelScope.launch {
            repository.getAllActiveReports().collect { reports ->
                _recentReports.value = reports.take(5)
            }
        }
    }
    
    fun getReportsByOfficer(officerName: String): Flow<List<BlotterReport>> {
        return repository.getReportsByOfficer(officerName)
    }
    
    fun getReportsByOfficerId(officerId: Int): Flow<List<BlotterReport>> {
        return repository.getReportsByOfficerId(officerId)
    }
    
    fun getReportsByStatus(status: String): Flow<List<BlotterReport>> {
        return repository.getReportsByStatus(status)
    }
    
    fun getNotifications(userId: Int): Flow<List<Notification>> {
        return repository.getNotificationsByUserId(userId)
    }
    
    fun getUnreadNotificationCount(userId: Int): Flow<Int> {
        return repository.getUnreadNotificationCount(userId)
    }
    
    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }
    
    fun refreshStats() {
        loadDashboardStats()
        loadRecentReports()
    }
    
    suspend fun insertReport(report: BlotterReport, createdBy: String, creatorUserId: Int = 0): Long {
        val reportId = repository.insertReport(report)
        
        // Log activity
        logActivity(
            activityType = "Report Filed",
            description = "New report filed: ${report.caseNumber} - ${report.incidentType}",
            performedBy = createdBy,
            caseId = reportId.toInt(),
            caseTitle = report.caseNumber
        )
        
        // Notify all admins about new report
        viewModelScope.launch {
            try {
                val admins = repository.getUsersByRole("Admin").first()
                admins.forEach { admin ->
                    val notification = com.example.blottermanagementsystem.data.entity.Notification(
                        userId = admin.id,
                        title = "New Report Filed",
                        message = "$createdBy filed a new report: ${report.caseNumber}",
                        type = "NEW_REPORT",
                        caseId = reportId.toInt(),
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertNotification(notification)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        refreshStats()
        return reportId
    }
    
    suspend fun updateReport(report: BlotterReport, updatedBy: String, oldStatus: String? = null) {
        repository.updateReport(report)
        
        // Log activity
        val description = if (oldStatus != null && oldStatus != report.status) {
            "Status changed from $oldStatus to ${report.status}"
        } else {
            "Report updated: ${report.caseNumber}"
        }
        
        logActivity(
            activityType = if (oldStatus != report.status) "Status Change" else "Report Updated",
            description = description,
            performedBy = updatedBy,
            caseId = report.id,
            caseTitle = report.caseNumber,
            oldValue = oldStatus,
            newValue = report.status
        )
        
        // Notify officer if status changed and officer is assigned
        if (oldStatus != null && oldStatus != report.status && !report.assignedOfficer.isNullOrEmpty()) {
            try {
                val officers = repository.getOfficerByName(report.assignedOfficer)
                officers?.let { officer ->
                    val notification = com.example.blottermanagementsystem.data.entity.Notification(
                        userId = officer.userId ?: 0,
                        title = "Report Status Updated",
                        message = "Case ${report.caseNumber} status changed to ${report.status}",
                        type = "STATUS_UPDATE",
                        caseId = report.id,
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertNotification(notification)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        refreshStats()
    }
    
    suspend fun deleteReport(report: BlotterReport, deletedBy: String) {
        repository.deleteReport(report)
        
        // Log activity
        logActivity(
            activityType = "Report Deleted",
            description = "Report deleted: ${report.caseNumber}",
            performedBy = deletedBy,
            caseId = report.id,
            caseTitle = report.caseNumber
        )
        
        refreshStats()
    }
    
    // Officer Management
    fun getAllOfficers() = repository.getAllOfficers()
    
    suspend fun getAllOfficersSync() = repository.getAllOfficersSync()
    
    suspend fun insertOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        repository.insertOfficer(officer)
        refreshStats()
    }
    
    suspend fun updateOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        repository.updateOfficer(officer)
        refreshStats()
    }
    suspend fun deleteOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        repository.deleteOfficer(officer)
        refreshStats()
    }
    
    // Witness Management
    suspend fun addWitness(witness: com.example.blottermanagementsystem.data.entity.Witness, addedBy: String, caseNumber: String, userRole: String = "") {
        // Backend validation: Only Officer and Clerk can add witnesses
        if (userRole == "Admin") {
            throw SecurityException("Admins cannot add witnesses. This action is restricted to Officers and Clerks.")
        }
        
        repository.insertWitness(witness)
        
        // Log activity
        logActivity(
            activityType = "Witness Added",
            description = "Witness added: ${witness.name} to case $caseNumber",
            performedBy = addedBy,
            caseId = witness.blotterReportId,
            caseTitle = caseNumber
        )
    }
    
    fun getWitnessesByReportId(reportId: Int) = repository.getWitnessesByReportId(reportId)
    
    // Suspect Management
    suspend fun addSuspect(suspect: com.example.blottermanagementsystem.data.entity.Suspect, addedBy: String, caseNumber: String, userRole: String = "") {
        // Backend validation: Only Officer and Clerk can add suspects
        if (userRole == "Admin") {
            throw SecurityException("Admins cannot add suspects. This action is restricted to Officers and Clerks.")
        }
        
        repository.insertSuspect(suspect)
        
        // Log activity
        logActivity(
            activityType = "Suspect Added",
            description = "Suspect added: ${suspect.name} to case $caseNumber",
            performedBy = addedBy,
            caseId = suspect.blotterReportId,
            caseTitle = caseNumber
        )
    }
    
    fun getSuspectsByReportId(reportId: Int) = repository.getSuspectsByReportId(reportId)
    
    // Evidence Management
    suspend fun addEvidence(evidence: com.example.blottermanagementsystem.data.entity.Evidence, addedBy: String, caseNumber: String, userRole: String = "") {
        // Backend validation: Only Officer and Clerk can add evidence
        if (userRole == "Admin") {
            throw SecurityException("Admins cannot add evidence. This action is restricted to Officers and Clerks.")
        }
        
        repository.insertEvidence(evidence)
        
        // Log activity
        logActivity(
            activityType = "Evidence Added",
            description = "Evidence added: ${evidence.evidenceType} to case $caseNumber",
            performedBy = addedBy,
            caseId = evidence.blotterReportId,
            caseTitle = caseNumber
        )
    }
    
    fun getEvidenceByReportId(reportId: Int) = repository.getEvidenceByReportId(reportId)
    
    // Hearing Management
    suspend fun addHearing(hearing: com.example.blottermanagementsystem.data.entity.Hearing, scheduledBy: String, caseNumber: String) {
        repository.insertHearing(hearing)
        
        // Log activity
        logActivity(
            activityType = "Hearing Scheduled",
            description = "Hearing scheduled for case $caseNumber on ${hearing.hearingDate}",
            performedBy = scheduledBy,
            caseId = hearing.blotterReportId,
            caseTitle = caseNumber
        )
    }
    
    fun getHearingsByReportId(reportId: Int) = repository.getHearingsByReportId(reportId)
    
    // Resolution Management
    suspend fun addResolution(resolution: com.example.blottermanagementsystem.data.entity.Resolution, addedBy: String, caseNumber: String) {
        repository.insertResolution(resolution)
        
        // Log activity
        logActivity(
            activityType = "Resolution Added",
            description = "Resolution added for case $caseNumber: ${resolution.resolutionType}",
            performedBy = addedBy,
            caseId = resolution.blotterReportId,
            caseTitle = caseNumber
        )
    }
    
    fun getResolutionByReportId(reportId: Int) = repository.getResolutionByReportId(reportId)
    
    // Activity Logs
    fun getRecentActivityLogs() = repository.getRecentActivityLogs()
    
    // All Hearings
    fun getAllHearings() = repository.getAllHearings()
    
    // Assign officer to report with notification
    suspend fun assignOfficerToReport(
        report: BlotterReport,
        officerName: String,
        officerUserId: Int,
        assignedBy: String
    ) {
        val updatedReport = report.copy(
            assignedOfficer = officerName,
            status = "Assigned"
        )
        repository.updateReport(updatedReport)
        
        // Log activity
        logActivity(
            activityType = "Officer Assigned",
            description = "Officer $officerName assigned to case ${report.caseNumber}",
            performedBy = assignedBy,
            caseId = report.id,
            caseTitle = report.caseNumber
        )
        
        // Notify the officer
        try {
            val notification = com.example.blottermanagementsystem.data.entity.Notification(
                userId = officerUserId,
                title = "New Case Assigned",
                message = "You have been assigned to case ${report.caseNumber}",
                type = "CASE_ASSIGNED",
                caseId = report.id,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertNotification(notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        refreshStats()
    }
    
    // Helper function to log activities
    private suspend fun logActivity(
        activityType: String,
        description: String,
        performedBy: String,
        caseId: Int? = null,
        caseTitle: String? = null,
        oldValue: String? = null,
        newValue: String? = null
    ) {
        try {
            val activityLog = ActivityLog(
                caseId = caseId,
                caseTitle = caseTitle,
                activityType = activityType,
                description = description,
                oldValue = oldValue,
                newValue = newValue,
                performedBy = performedBy,
                timestamp = System.currentTimeMillis()
            )
            repository.insertActivityLog(activityLog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
