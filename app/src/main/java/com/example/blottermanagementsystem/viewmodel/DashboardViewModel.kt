package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.*
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import com.example.blottermanagementsystem.utils.SecurityUtils
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
        mediationSessionDao = database.mediationSessionDao(),
        caseTimelineDao = database.caseTimelineDao(),
        caseTemplateDao = database.caseTemplateDao()
    )
    
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats
    
    private val _allReports = MutableStateFlow<List<BlotterReport>>(emptyList())
    val allReports: StateFlow<List<BlotterReport>> = _allReports
    
    val allUsers: Flow<List<User>> = repository.getAllUsers()
    
    private val _recentReports = MutableStateFlow<List<BlotterReport>>(emptyList())
    val recentReports: StateFlow<List<BlotterReport>> = _recentReports
    
    init {
        loadDashboardStats()
        loadRecentReports()
        loadAllReports()
    }
    
    private fun loadAllReports() {
        viewModelScope.launch {
            try {
                // Load from local Room database - collect ONCE
                Log.d("DashboardViewModel", "Fetching all reports from local database...")
                _allReports.value = repository.getAllActiveReports().first()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading reports", e)
                _allReports.value = emptyList()
            }
        }
    }
    
    private fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                // Load stats from local Room database
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
                Log.e("DashboardViewModel", "Error loading stats: ${e.message}")
            }
        }
    }
    
    
    private fun loadRecentReports() {
        viewModelScope.launch {
            try {
                val reports = repository.getAllActiveReports().first()
                _recentReports.value = reports.take(5)
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading recent reports", e)
                _recentReports.value = emptyList()
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
    
    // Add refresh function for pull-to-refresh
    fun refreshDashboard() {
        loadDashboardStats()
        loadRecentReports()
        loadAllReports()
    }
    
    suspend fun insertReport(report: BlotterReport, createdBy: String, creatorUserId: Int = 0): Long {
        // Save to local Room database
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
        // Update local Room database
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
        // Delete from local Room database
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
        // Prevent admins from adding witnesses
        if (userRole == "Admin") {
            throw SecurityException("Admins cannot add witnesses. This action is restricted to Officers and Clerks.")
        }
        
        // Save to local Room database
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
        
        // Save to local Room database
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
        
        // Save to local Room database
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
        // Save to local Room database
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
        // Save to local Room database
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
    
    // Assign multiple officers to case
    fun assignOfficersToCase(reportId: Int, officerIds: List<Int>) {
        viewModelScope.launch {
            try {
                val report = repository.getReportById(reportId)
                if (report != null) {
                    val officerIdsString = officerIds.joinToString(",")
                    val updatedReport = report.copy(
                        assignedOfficerIds = officerIdsString,
                        status = if (report.status == "Pending") "Assigned" else report.status
                    )
                    repository.updateReport(updatedReport)
                    
                    // Log activity
                    logActivity(
                        activityType = "Officers Assigned",
                        description = "Officers assigned to case ${report.caseNumber}",
                        performedBy = "Admin",
                        caseId = report.id,
                        caseTitle = report.caseNumber
                    )
                    
                    // Notify each officer
                    officerIds.forEach { officerId ->
                        try {
                            val notification = com.example.blottermanagementsystem.data.entity.Notification(
                                userId = officerId,
                                title = "New Case Assigned",
                                message = "You have been assigned to case ${report.caseNumber}",
                                type = "CASE_ASSIGNED",
                                caseId = report.id,
                                isRead = false,
                                timestamp = System.currentTimeMillis()
                            )
                            repository.insertNotification(notification)
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "Failed to notify officer $officerId: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Failed to assign officers: ${e.message}")
            }
        }
    }
    
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
            
            // ⚡ Try API first (sync activity log to cloud)
            try {
                Log.d("DashboardViewModel", "Logging activity to API...")
                val logData = mapOf(
                    "case_id" to (caseId?.toString() ?: ""),
                    "case_title" to (caseTitle ?: ""),
                    "activity_type" to activityType,
                    "description" to description,
                    "old_value" to (oldValue ?: ""),
                    "new_value" to (newValue ?: ""),
                    "performed_by" to performedBy,
                    "timestamp" to activityLog.timestamp.toString()
                )
                // Note: API endpoint for activity logs needs to be added to backend
                // For now, just log locally
                Log.d("DashboardViewModel", "Activity log ready for API sync")
            } catch (e: Exception) {
                Log.w("DashboardViewModel", "API logging skipped: ${e.message}")
            }
            
            // Always save to local Room database (backup + offline support)
            repository.insertActivityLog(activityLog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Case Timeline operations
    fun getCaseTimeline(reportId: Int) = repository.getTimelineByReportId(reportId)
    
    suspend fun addTimelineEvent(
        reportId: Int,
        eventType: String,
        eventTitle: String,
        eventDescription: String,
        performedBy: String,
        performedByRole: String
    ) {
        val event = com.example.blottermanagementsystem.data.entity.CaseTimeline(
            blotterReportId = reportId,
            eventType = eventType,
            eventTitle = eventTitle,
            eventDescription = eventDescription,
            performedBy = performedBy,
            performedByRole = performedByRole
        )
        repository.insertTimelineEvent(event)
    }
    
    // Case Template operations
    fun getAllTemplates() = repository.getAllActiveTemplates()
    
    suspend fun getTemplateById(templateId: Int) = repository.getTemplateById(templateId)
    
    suspend fun useTemplate(templateId: Int) {
        repository.incrementTemplateUsage(templateId)
    }
    
    // User Management
    suspend fun toggleUserStatus(userId: Int) {
        val user = repository.getUserById(userId)
        user?.let {
            val updatedUser = it.copy(isActive = !it.isActive)
            repository.updateUser(updatedUser)
        }
    }
    
    suspend fun deleteUser(userId: Int) {
        val user = repository.getUserById(userId)
        user?.let {
            repository.deleteUser(it)
        }
    }
    
    // Officer Management (Admin functions)
    suspend fun createOfficerAccount(
        firstName: String,
        lastName: String,
        username: String,
        tempPassword: String,
        badgeNumber: String
    ): Int {
        val hashedPassword = com.example.blottermanagementsystem.utils.SecurityUtils.hashPassword(tempPassword)
        
        val newUser = com.example.blottermanagementsystem.data.entity.User(
            firstName = firstName,
            lastName = lastName,
            username = username,
            password = hashedPassword,
            role = "Officer",
            profileCompleted = false,
            mustChangePassword = true,
            badgeNumber = badgeNumber,
            isActive = true
        )
        
        return repository.insertUser(newUser).toInt()
    }
    
    suspend fun addOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        repository.insertOfficer(officer)
        refreshStats()
    }
    
    suspend fun deleteOfficerById(officerId: Int) {
        val allOfficers = repository.getAllOfficersSync()
        val officer = allOfficers.find { it.id == officerId }
        officer?.let {
            repository.deleteOfficer(it)
            refreshStats()
        }
    }
    
    suspend fun getAssignedOfficerIds(reportId: Int): List<Int> {
        val report = repository.getReportById(reportId)
        return report?.assignedOfficerIds
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?: emptyList()
    }
    
    suspend fun updateReportStatus(
        reportId: Int,
        newStatus: String,
        notes: String,
        userId: Int
    ) {
        val report = repository.getReportById(reportId)
        report?.let {
            val updatedReport = it.copy(status = newStatus)
            repository.updateReport(updatedReport)
            
            logActivity(
                activityType = "Status Updated",
                description = "Case ${it.caseNumber} status changed to $newStatus. Notes: $notes",
                performedBy = "User $userId",
                caseId = reportId,
                caseTitle = it.caseNumber
            )
            
            refreshStats()
        }
    }
    
    suspend fun addResolution(resolution: com.example.blottermanagementsystem.data.entity.Resolution) {
        repository.insertResolution(resolution)
        refreshStats()
    }
}


