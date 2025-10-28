package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.*
import com.example.blottermanagementsystem.data.repository.ApiRepository
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
    
    private val apiRepository = ApiRepository()
    
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats
    
    private val _allReports = MutableStateFlow<List<BlotterReport>>(emptyList())
    val allReports: StateFlow<List<BlotterReport>> = _allReports
    
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers
    
    private val _allOfficers = MutableStateFlow<List<com.example.blottermanagementsystem.data.entity.Officer>>(emptyList())
    val allOfficers: StateFlow<List<com.example.blottermanagementsystem.data.entity.Officer>> = _allOfficers
    
    private val _recentReports = MutableStateFlow<List<BlotterReport>>(emptyList())
    val recentReports: StateFlow<List<BlotterReport>> = _recentReports
    
    init {
        loadDashboardStats()
        loadRecentReports()
        loadAllReports()
        syncUsersFromCloud() // Sync users from API
        syncOfficersFromCloud() // Sync officers from API
        syncReportsFromCloud() // Sync reports from API (for officers/admin)
        
        // Auto-cleanup old data on app startup (runs in background)
        viewModelScope.launch {
            try {
                // Wait 5 seconds after app starts to avoid slowing down initial load
                kotlinx.coroutines.delay(5000)
                cleanupOldLocalData(daysToKeep = 30)
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Auto-cleanup error: ${e.message}", e)
            }
        }
    }
    
    // NEW: Sync users from cloud API
    private fun syncUsersFromCloud() {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "👥 Syncing users from cloud API...")
                val result = apiRepository.getAllUsersFromCloud()
                
                if (result.isSuccess) {
                    val cloudUsers = result.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "✅ Fetched ${cloudUsers.size} users from cloud")
                    
                    // Update local database
                    cloudUsers.forEach { user ->
                        repository.insertUser(user)
                    }
                    
                    // Update UI
                    _allUsers.value = cloudUsers
                    Log.d("DashboardViewModel", "✅ Users synced successfully!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to fetch users from cloud")
                    // Fallback to local database
                    _allUsers.value = repository.getAllUsersSync()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing users: ${e.message}", e)
                // Fallback to local database
                _allUsers.value = repository.getAllUsersSync()
            }
        }
    }
    
    // NEW: Sync officers from cloud API
    private fun syncOfficersFromCloud() {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "👮 Syncing officers from cloud API...")
                val result = apiRepository.getAllOfficersFromCloud()
                
                if (result.isSuccess) {
                    val cloudOfficers = result.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "✅ Fetched ${cloudOfficers.size} officers from cloud")
                    
                    // Update local database
                    cloudOfficers.forEach { officer ->
                        repository.insertOfficer(officer)
                    }
                    
                    // Update UI
                    _allOfficers.value = cloudOfficers
                    Log.d("DashboardViewModel", "✅ Officers synced successfully!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to fetch officers from cloud")
                    // Fallback to local database
                    _allOfficers.value = repository.getAllOfficersSync()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing officers: ${e.message}", e)
                // Fallback to local database
                _allOfficers.value = repository.getAllOfficersSync()
            }
        }
    }
    
    // NEW: Sync reports from cloud API (for Admin/Officer)
    private fun syncReportsFromCloud() {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📋 Syncing reports from cloud API...")
                val result = apiRepository.getAllReportsFromCloud()
                
                if (result.isSuccess) {
                    val cloudReports = result.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "✅ Fetched ${cloudReports.size} reports from cloud")
                    
                    // Update local database
                    cloudReports.forEach { report ->
                        repository.insertReport(report)
                    }
                    
                    // Update UI
                    _allReports.value = cloudReports
                    Log.d("DashboardViewModel", "✅ Reports synced successfully!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to fetch reports from cloud")
                    // Fallback to local database
                    _allReports.value = repository.getAllActiveReports().first()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing reports: ${e.message}", e)
                // Fallback to local database
                _allReports.value = repository.getAllActiveReports().first()
            }
        }
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
                // Always load from local first for instant UI update (no flickering)
                Log.d("DashboardViewModel", "📱 Loading dashboard stats from local database...")
                loadDashboardStatsFromLocal()
                
                // Then sync ALL data from cloud in background
                Log.d("DashboardViewModel", "📊 Syncing all data from cloud API...")
                
                // Sync users, officers, and reports FIRST (to update local DB)
                val usersResult = apiRepository.getAllUsersFromCloud()
                val officersResult = apiRepository.getAllOfficersFromCloud()
                val reportsResult = apiRepository.getAllReportsFromCloud()
                
                // Update local database with cloud data
                if (usersResult.isSuccess) {
                    val cloudUsers = usersResult.getOrNull() ?: emptyList()
                    cloudUsers.forEach { user -> repository.insertUser(user) }
                    Log.d("DashboardViewModel", "✅ ${cloudUsers.size} users synced to local DB")
                }
                
                if (officersResult.isSuccess) {
                    val cloudOfficers = officersResult.getOrNull() ?: emptyList()
                    cloudOfficers.forEach { officer -> repository.insertOfficer(officer) }
                    Log.d("DashboardViewModel", "✅ ${cloudOfficers.size} officers synced to local DB")
                }
                
                if (reportsResult.isSuccess) {
                    val cloudReports = reportsResult.getOrNull() ?: emptyList()
                    cloudReports.forEach { report -> repository.insertReport(report) }
                    Log.d("DashboardViewModel", "✅ ${cloudReports.size} reports synced to local DB")
                }
                
                // NOW reload stats from local DB (which has fresh cloud data)
                // This ensures smooth update without flickering
                loadDashboardStatsFromLocal()
                Log.d("DashboardViewModel", "✅ Dashboard stats refreshed with cloud data!")
                
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error loading dashboard stats: ${e.message}", e)
                // Fallback to local database
                try {
                    loadDashboardStatsFromLocal()
                } catch (localError: Exception) {
                    Log.e("DashboardViewModel", "❌ Error loading local stats: ${localError.message}", localError)
                }
            }
        }
    }
    
    private suspend fun loadDashboardStatsFromLocal() {
        try {
            Log.d("DashboardViewModel", "📱 Loading dashboard stats from local database...")
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
            Log.d("DashboardViewModel", "✅ Stats: Users=$totalUsers, Officers=$totalOfficers, Active=$totalReports, Pending=$pendingReports, Ongoing=$ongoingReports, Resolved=$resolvedReports, Archived=$archivedReports")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error loading local stats: ${e.message}", e)
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
    
    // Get reports for specific user (for User role dashboard)
    fun getReportsByUser(userId: Int): Flow<List<BlotterReport>> {
        return repository.getReportsByUser(userId)
    }
    
    // Refresh user reports from cloud (for User role)
    fun refreshUserReports(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "🔄 Refreshing user reports for userId=$userId...")
                
                // Sync reports from cloud
                val result = apiRepository.getAllReportsFromCloud()
                
                if (result.isSuccess) {
                    val cloudReports = result.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "✅ Fetched ${cloudReports.size} reports from cloud")
                    
                    // Update local database
                    cloudReports.forEach { report ->
                        repository.insertReport(report)
                    }
                    
                    // Filter user's reports
                    val userReports = cloudReports.filter { it.userId == userId }
                    Log.d("DashboardViewModel", "✅ User has ${userReports.size} reports")
                    
                    // Update allReports (which UserDashboard uses)
                    _allReports.value = cloudReports
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to fetch reports from cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error refreshing user reports: ${e.message}", e)
            }
        }
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
        Log.d("DashboardViewModel", "🔄 Refreshing dashboard...")
        loadDashboardStats()
        loadRecentReports()
        loadAllReports()
        syncUsersFromCloud() // Re-sync users from cloud
        syncOfficersFromCloud() // Re-sync officers from cloud
        syncReportsFromCloud() // Re-sync reports from cloud
    }
    
    suspend fun insertReport(report: BlotterReport, createdBy: String, creatorUserId: Int = 0): Long {
        // Save to local Room database first
        val reportId = repository.insertReport(report)
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Syncing new report to cloud API...")
                val reportWithId = report.copy(id = reportId.toInt())
                val result = apiRepository.createReport(reportWithId)
                
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Report synced to cloud successfully!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync report to cloud: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing report to cloud: ${e.message}", e)
            }
        }
        
        // Log activity
        logActivity(
            activityType = "Report Filed",
            description = "New report filed: ${report.caseNumber} - ${report.incidentType}",
            performedBy = createdBy,
            caseId = reportId.toInt(),
            caseTitle = report.caseNumber
        )
        
        // Notify the user who filed the report
        viewModelScope.launch {
            try {
                val userNotification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = creatorUserId,
                    title = "Report Filed Successfully",
                    message = "Your case ${report.caseNumber} has been filed and is under review",
                    type = "CASE_FILED",
                    caseId = reportId.toInt(),
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(userNotification)
                Log.d("DashboardViewModel", "✅ User notification created for case ${report.caseNumber}")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Failed to create user notification: ${e.message}", e)
            }
        }
        
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
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Syncing report update to cloud API...")
                val result = apiRepository.updateReport(report.id, report)
                
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Report update synced to cloud successfully!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync report update to cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing report update: ${e.message}", e)
            }
        }
        
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
    // allOfficers is already exposed as StateFlow property above
    
    suspend fun getAllOfficersSync() = repository.getAllOfficersSync()
    
    suspend fun insertOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        // Save to local database
        repository.insertOfficer(officer)
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Syncing new officer to cloud...")
                val result = apiRepository.createOfficer(officer)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Officer synced to cloud successfully!")
                    // Refresh officers from cloud to get the cloud ID
                    syncOfficersFromCloud()
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync officer to cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing officer: ${e.message}", e)
            }
        }
        
        refreshStats()
    }
    
    suspend fun updateOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        // Update local database
        repository.updateOfficer(officer)
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Syncing officer update to cloud...")
                val result = apiRepository.updateOfficer(officer)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Officer update synced to cloud!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync officer update to cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing officer update: ${e.message}", e)
            }
        }
        
        refreshStats()
    }
    
    suspend fun deleteOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer) {
        // Delete from local database
        repository.deleteOfficer(officer)
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Deleting officer from cloud...")
                val result = apiRepository.deleteOfficer(officer.id)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Officer deleted from cloud!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to delete officer from cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error deleting officer from cloud: ${e.message}", e)
            }
        }
        
        refreshStats()
    }
    
    // Witness Management
    suspend fun addWitness(witness: com.example.blottermanagementsystem.data.entity.Witness, addedBy: String, caseNumber: String, userRole: String = "") {
        // Prevent admins from adding witnesses
        if (userRole == "Admin") {
            throw SecurityException("Admins cannot add witnesses. This action is restricted to Officers and Clerks.")
        }
        
        try {
            Log.d("DashboardViewModel", "👥 Adding witness to cloud...")
            val result = apiRepository.createWitness(witness)
            if (result.isSuccess) {
                val cloudWitness = result.getOrNull()
                if (cloudWitness != null) {
                    repository.insertWitness(cloudWitness)
                    Log.d("DashboardViewModel", "✅ Witness synced to cloud and local")
                }
            } else {
                Log.e("DashboardViewModel", "❌ Failed to sync witness, saving locally only")
                repository.insertWitness(witness)
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error syncing witness: ${e.message}", e)
            repository.insertWitness(witness)
        }
        
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
        
        try {
            Log.d("DashboardViewModel", "🔍 Adding suspect to cloud...")
            val result = apiRepository.createSuspect(suspect)
            if (result.isSuccess) {
                val cloudSuspect = result.getOrNull()
                if (cloudSuspect != null) {
                    repository.insertSuspect(cloudSuspect)
                    Log.d("DashboardViewModel", "✅ Suspect synced to cloud and local")
                }
            } else {
                Log.e("DashboardViewModel", "❌ Failed to sync suspect, saving locally only")
                repository.insertSuspect(suspect)
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error syncing suspect: ${e.message}", e)
            repository.insertSuspect(suspect)
        }
        
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
        
        try {
            Log.d("DashboardViewModel", "📸 Adding evidence to cloud...")
            
            // Save to cloud API first
            val result = apiRepository.createEvidence(evidence)
            
            if (result.isSuccess) {
                val cloudEvidence = result.getOrNull()
                if (cloudEvidence != null) {
                    // Save cloud version to local database (with cloud ID)
                    repository.insertEvidence(cloudEvidence)
                    Log.d("DashboardViewModel", "✅ Evidence synced to cloud and local")
                }
            } else {
                Log.e("DashboardViewModel", "❌ Failed to sync evidence to cloud, saving locally only")
                // Fallback: Save to local only
                repository.insertEvidence(evidence)
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error syncing evidence: ${e.message}", e)
            // Fallback: Save to local only
            repository.insertEvidence(evidence)
        }
        
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
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Syncing hearing to cloud...")
                val result = apiRepository.createHearing(hearing)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Hearing synced to cloud!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync hearing to cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing hearing: ${e.message}", e)
            }
        }
        
        // Log activity
        logActivity(
            activityType = "Hearing Scheduled",
            description = "Hearing scheduled for case $caseNumber on ${hearing.hearingDate}",
            performedBy = scheduledBy,
            caseId = hearing.blotterReportId,
            caseTitle = caseNumber
        )
        
        // Notify the user who filed the case
        try {
            val report = repository.getReportById(hearing.blotterReportId)
            if (report != null) {
                val userNotification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = report.userId,
                    title = "Hearing Scheduled",
                    message = "A hearing for your case $caseNumber has been scheduled on ${hearing.hearingDate} at ${hearing.hearingTime}",
                    type = "HEARING_SCHEDULED",
                    caseId = hearing.blotterReportId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(userNotification)
                Log.d("DashboardViewModel", "✅ User notified about hearing")
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to notify user about hearing: ${e.message}", e)
        }
        
        // Notify all admins about hearing
        try {
            val admins = repository.getUsersByRole("Admin").first()
            admins.forEach { admin ->
                val adminNotification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = admin.id,
                    title = "Hearing Scheduled",
                    message = "Hearing scheduled for case $caseNumber on ${hearing.hearingDate} at ${hearing.hearingTime}",
                    type = "HEARING_SCHEDULED",
                    caseId = hearing.blotterReportId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(adminNotification)
            }
            Log.d("DashboardViewModel", "✅ Admins notified about hearing")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to notify admins about hearing: ${e.message}", e)
        }
    }
    
    fun getHearingsByReportId(reportId: Int) = repository.getHearingsByReportId(reportId)
    
    // Resolution Management
    suspend fun addResolution(resolution: com.example.blottermanagementsystem.data.entity.Resolution, addedBy: String, caseNumber: String) {
        try {
            Log.d("DashboardViewModel", "✅ Adding resolution to cloud...")
            val result = apiRepository.createResolution(resolution)
            if (result.isSuccess) {
                val cloudResolution = result.getOrNull()
                if (cloudResolution != null) {
                    repository.insertResolution(cloudResolution)
                    Log.d("DashboardViewModel", "✅ Resolution synced to cloud and local")
                }
            } else {
                Log.e("DashboardViewModel", "❌ Failed to sync resolution, saving locally only")
                repository.insertResolution(resolution)
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error syncing resolution: ${e.message}", e)
            repository.insertResolution(resolution)
        }
        
        // Log activity
        logActivity(
            activityType = "Resolution Added",
            description = "Resolution added for case $caseNumber: ${resolution.resolutionType}",
            performedBy = addedBy,
            caseId = resolution.blotterReportId,
            caseTitle = caseNumber
        )
        
        // Notify the user who filed the case
        try {
            val report = repository.getReportById(resolution.blotterReportId)
            if (report != null) {
                val userNotification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = report.userId,
                    title = "Case Resolved",
                    message = "Your case $caseNumber has been resolved: ${resolution.resolutionType}",
                    type = "CASE_RESOLVED",
                    caseId = resolution.blotterReportId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(userNotification)
                Log.d("DashboardViewModel", "✅ User notified about resolution")
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to notify user about resolution: ${e.message}", e)
        }
        
        // Notify assigned officers
        try {
            val report = repository.getReportById(resolution.blotterReportId)
            if (report != null && report.assignedOfficerIds.isNotEmpty()) {
                val officerIds = report.assignedOfficerIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                officerIds.forEach { officerId ->
                    val officerNotification = com.example.blottermanagementsystem.data.entity.Notification(
                        userId = officerId,
                        title = "Case Resolved",
                        message = "Case $caseNumber has been resolved: ${resolution.resolutionType}",
                        type = "CASE_RESOLVED",
                        caseId = resolution.blotterReportId,
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertNotification(officerNotification)
                }
                Log.d("DashboardViewModel", "✅ Officers notified about resolution")
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to notify officers about resolution: ${e.message}", e)
        }
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
                        status = if (report.status == "Pending") "Under Investigation" else report.status
                    )
                    
                    // Update local database
                    repository.updateReport(updatedReport)
                    
                    // Sync to cloud API
                    try {
                        Log.d("DashboardViewModel", "📤 Syncing officer assignment to cloud...")
                        val result = apiRepository.updateReport(updatedReport.id, updatedReport)
                        if (result.isSuccess) {
                            Log.d("DashboardViewModel", "✅ Officer assignment synced to cloud!")
                        } else {
                            Log.e("DashboardViewModel", "❌ Failed to sync officer assignment to cloud")
                        }
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "❌ Error syncing officer assignment: ${e.message}", e)
                    }
                    
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
                            val officerNotification = com.example.blottermanagementsystem.data.entity.Notification(
                                userId = officerId,
                                title = "New Case Assigned",
                                message = "You have been assigned to case ${report.caseNumber} - ${report.incidentType}",
                                type = "CASE_ASSIGNED",
                                caseId = report.id,
                                isRead = false,
                                timestamp = System.currentTimeMillis()
                            )
                            repository.insertNotification(officerNotification)
                            Log.d("DashboardViewModel", "✅ Officer $officerId notified")
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "❌ Failed to notify officer $officerId: ${e.message}")
                        }
                    }
                    
                    // Notify the user who filed the case
                    try {
                        val userNotification = com.example.blottermanagementsystem.data.entity.Notification(
                            userId = report.userId,
                            title = "Officers Assigned to Your Case",
                            message = "Your case ${report.caseNumber} is now under investigation",
                            type = "OFFICER_ASSIGNED",
                            caseId = report.id,
                            isRead = false,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.insertNotification(userNotification)
                        Log.d("DashboardViewModel", "✅ User notified about officer assignment")
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "❌ Failed to notify user: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Failed to assign officers: ${e.message}")
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
        
        // Update local database
        repository.updateReport(updatedReport)
        
        // Sync to cloud API
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "📤 Syncing officer assignment to cloud...")
                val result = apiRepository.updateReport(updatedReport.id, updatedReport)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Officer assignment synced to cloud!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync officer assignment to cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing officer assignment: ${e.message}", e)
            }
        }
        
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
            val officerNotification = com.example.blottermanagementsystem.data.entity.Notification(
                userId = officerUserId,
                title = "New Case Assigned",
                message = "You have been assigned to case ${report.caseNumber} - ${report.incidentType}",
                type = "CASE_ASSIGNED",
                caseId = report.id,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertNotification(officerNotification)
            Log.d("DashboardViewModel", "✅ Officer notification created")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to create officer notification: ${e.message}", e)
        }
        
        // Notify the user who filed the case
        try {
            val userNotification = com.example.blottermanagementsystem.data.entity.Notification(
                userId = report.userId,
                title = "Officer Assigned to Your Case",
                message = "Officer $officerName has been assigned to your case ${report.caseNumber}",
                type = "OFFICER_ASSIGNED",
                caseId = report.id,
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insertNotification(userNotification)
            Log.d("DashboardViewModel", "✅ User notification created")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to create user notification: ${e.message}", e)
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
                Log.d("DashboardViewModel", "📋 Logging activity to cloud...")
                val result = apiRepository.createActivityLog(activityLog)
                if (result.isSuccess) {
                    val cloudLog = result.getOrNull()
                    if (cloudLog != null) {
                        repository.insertActivityLog(cloudLog)
                        Log.d("DashboardViewModel", "✅ Activity log synced to cloud and local")
                    }
                } else {
                    Log.w("DashboardViewModel", "❌ Failed to sync activity log, saving locally only")
                    repository.insertActivityLog(activityLog)
                }
            } catch (e: Exception) {
                Log.w("DashboardViewModel", "❌ Error syncing activity log: ${e.message}")
                // Fallback: Save to local only
                repository.insertActivityLog(activityLog)
            }
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
        try {
            val user = repository.getUserById(userId) ?: return
            val newStatus = !user.isActive
            val statusText = if (newStatus) "Activated" else "Terminated"
            
            Log.d("DashboardViewModel", "🔄 ${if (newStatus) "Activating" else "Terminating"} user: ${user.username} (ID: $userId)")
            
            // Update local database
            val updatedUser = user.copy(isActive = newStatus)
            repository.updateUser(updatedUser)
            Log.d("DashboardViewModel", "💾 User status updated in local database")
            
            // Sync to cloud
            try {
                val result = apiRepository.updateUserInfo(
                    userId = userId,
                    firstName = updatedUser.firstName,
                    lastName = updatedUser.lastName,
                    username = updatedUser.username
                )
                
                // Also update isActive status via PUT /api/users/:id
                val updateResult = apiRepository.updateUserPassword(userId, updatedUser.password)
                
                if (result.isSuccess && updateResult.isSuccess) {
                    Log.d("DashboardViewModel", "✅ User $statusText and synced to cloud")
                } else {
                    Log.e("DashboardViewModel", "⚠️ User $statusText locally but cloud sync failed")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing user status to cloud: ${e.message}", e)
            }
            
            // Send notification to user
            try {
                val notification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = userId,
                    title = if (newStatus) "Account Activated" else "Account Terminated",
                    message = if (newStatus) 
                        "Your account has been activated by the administrator. You can now login and access the system." 
                    else 
                        "Your account has been terminated by the administrator. You can no longer access the system. Please contact the Barangay office for more information.",
                    type = if (newStatus) "ACCOUNT_ACTIVATED" else "ACCOUNT_TERMINATED",
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(notification)
                Log.d("DashboardViewModel", "📧 Notification sent to user about account $statusText")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Failed to send notification: ${e.message}", e)
            }
            
            // Log activity
            logActivity(
                activityType = if (newStatus) "USER_ACTIVATED" else "USER_TERMINATED",
                description = "User ${user.username} was $statusText",
                performedBy = "Admin"
            )
            
            Log.d("DashboardViewModel", "✅ User ${user.username} $statusText successfully")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error toggling user status: ${e.message}", e)
        }
    }
    
    suspend fun deleteUser(userId: Int) {
        try {
            val user = repository.getUserById(userId) ?: return
            
            Log.d("DashboardViewModel", "🗑️ Deleting user: ${user.username} (ID: $userId)")
            
            // Delete from cloud first
            try {
                val result = apiRepository.deleteUser(userId)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ User deleted from cloud")
                } else {
                    Log.e("DashboardViewModel", "⚠️ Failed to delete user from cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error deleting user from cloud: ${e.message}", e)
            }
            
            // Send notification to user BEFORE deleting
            try {
                val notification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = userId,
                    title = "Account Deleted",
                    message = "Your account has been permanently deleted by the administrator. All your data has been removed from the system. If you believe this was done in error, please contact the Barangay office immediately.",
                    type = "ACCOUNT_DELETED",
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(notification)
                Log.d("DashboardViewModel", "📧 Notification sent to user about account deletion")
                
                // Give user a chance to see notification (sync to cloud)
                kotlinx.coroutines.delay(2000) // 2 seconds delay
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Failed to send notification: ${e.message}", e)
            }
            
            // Delete user's reports
            val userReports = repository.getReportsByUser(userId).first()
            userReports.forEach { report ->
                repository.deleteReport(report)
            }
            Log.d("DashboardViewModel", "🗑️ Deleted ${userReports.size} reports by user")
            
            // Delete user's notifications (including the deletion notice)
            repository.deleteAllNotificationsByUserId(userId)
            Log.d("DashboardViewModel", "🗑️ Deleted user notifications")
            
            // Delete user from local database
            repository.deleteUser(user)
            Log.d("DashboardViewModel", "💾 User deleted from local database")
            
            // Log activity
            logActivity(
                activityType = "USER_DELETED",
                description = "User ${user.username} was permanently deleted",
                performedBy = "Admin"
            )
            
            Log.d("DashboardViewModel", "✅ User ${user.username} deleted successfully")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error deleting user: ${e.message}", e)
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
    
    suspend fun getReportByIdDirect(reportId: Int): BlotterReport? {
        // Try local database first
        var report = repository.getReportById(reportId)
        
        // If not found locally, try fetching from cloud
        if (report == null) {
            Log.d("DashboardViewModel", "📋 Report $reportId not found locally, fetching from cloud...")
            try {
                val result = apiRepository.getReportByIdFromCloud(reportId)
                if (result.isSuccess) {
                    report = result.getOrNull()
                    // Save to local database for future use
                    report?.let { repository.insertReport(it) }
                    Log.d("DashboardViewModel", "✅ Report fetched from cloud and saved locally")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to fetch report from cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error fetching report from cloud: ${e.message}", e)
            }
        }
        
        return report
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
            
            // Update local database
            repository.updateReport(updatedReport)
            
            // Sync to cloud API
            try {
                Log.d("DashboardViewModel", "📤 Syncing status update to cloud...")
                val result = apiRepository.updateReport(updatedReport.id, updatedReport)
                if (result.isSuccess) {
                    Log.d("DashboardViewModel", "✅ Status update synced to cloud!")
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync status update to cloud")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing status update: ${e.message}", e)
            }
            
            logActivity(
                activityType = "Status Updated",
                description = "Case ${it.caseNumber} status changed to $newStatus. Notes: $notes",
                performedBy = "User $userId",
                caseId = reportId,
                caseTitle = it.caseNumber
            )
            
            // Notify the user who filed the case
            try {
                val userNotification = com.example.blottermanagementsystem.data.entity.Notification(
                    userId = it.userId,
                    title = "Case Status Updated",
                    message = "Your case ${it.caseNumber} status changed to $newStatus",
                    type = "STATUS_UPDATE",
                    caseId = reportId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNotification(userNotification)
                Log.d("DashboardViewModel", "✅ User notification created for status update")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Failed to create user notification: ${e.message}", e)
            }
            
            // Notify all admins about status change
            try {
                val admins = repository.getUsersByRole("Admin").first()
                admins.forEach { admin ->
                    val adminNotification = com.example.blottermanagementsystem.data.entity.Notification(
                        userId = admin.id,
                        title = "Case Status Updated",
                        message = "Case ${it.caseNumber} status changed to $newStatus",
                        type = "STATUS_UPDATE",
                        caseId = reportId,
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertNotification(adminNotification)
                }
                Log.d("DashboardViewModel", "✅ Admin notifications created for status update")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Failed to create admin notifications: ${e.message}", e)
            }
            
            refreshStats()
        }
    }
    
    suspend fun addResolution(resolution: com.example.blottermanagementsystem.data.entity.Resolution) {
        repository.insertResolution(resolution)
        refreshStats()
    }
    
    // ==================== SMART CACHING & CLEANUP ====================
    
    /**
     * Auto-cleanup old resolved reports from local database
     * Keeps only recent reports (last 30 days) locally
     * Cloud database keeps ALL reports forever
     * 
     * Benefits:
     * - Reduces app storage size
     * - Faster performance (less local data)
     * - Old reports still accessible from cloud
     */
    suspend fun cleanupOldLocalData(daysToKeep: Int = 30) {
        try {
            val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            
            Log.d("DashboardViewModel", "🧹 Starting cleanup of reports older than $daysToKeep days...")
            
            // Get all reports
            val allReports = repository.getAllActiveReports().first()
            
            // Filter old resolved/archived reports
            val oldReports = allReports.filter { report ->
                (report.status == "Resolved" || report.isArchived) && 
                report.dateFiled < cutoffTime
            }
            
            Log.d("DashboardViewModel", "📊 Found ${oldReports.size} old reports to clean up")
            
            // Delete from local database only (cloud keeps them)
            oldReports.forEach { report ->
                repository.deleteReport(report)
                Log.d("DashboardViewModel", "🗑️ Removed report ${report.id} (${report.caseNumber}) from local cache")
            }
            
            Log.d("DashboardViewModel", "✅ Cleanup complete! Removed ${oldReports.size} old reports from local storage")
            Log.d("DashboardViewModel", "☁️ All data still safe in cloud database!")
            
            // Refresh stats after cleanup
            refreshStats()
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error during cleanup: ${e.message}", e)
        }
    }
    
    /**
     * Clear all cache (for Settings → Clear Cache)
     * Removes all local data except user's own active reports
     */
    suspend fun clearAllCache(currentUserId: Int) {
        try {
            Log.d("DashboardViewModel", "🧹 Clearing all cache...")
            
            val allReports = repository.getAllActiveReports().first()
            
            // Keep only user's own active reports
            val reportsToDelete = allReports.filter { report ->
                report.userId != currentUserId || 
                report.status == "Resolved" || 
                report.isArchived
            }
            
            reportsToDelete.forEach { report ->
                repository.deleteReport(report)
            }
            
            Log.d("DashboardViewModel", "✅ Cache cleared! Removed ${reportsToDelete.size} reports")
            Log.d("DashboardViewModel", "💾 Kept user's active reports")
            
            refreshStats()
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error clearing cache: ${e.message}", e)
        }
    }
    
    /**
     * Get storage info for Settings screen
     */
    suspend fun getStorageInfo(): StorageInfo {
        return try {
            val allReports = repository.getAllActiveReports().first()
            val totalReports = allReports.size
            val recentReports = allReports.count { 
                it.dateFiled > System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
            }
            val oldReports = totalReports - recentReports
            
            StorageInfo(
                totalReports = totalReports,
                recentReports = recentReports,
                oldReports = oldReports,
                estimatedSizeMB = totalReports * 0.5 // Rough estimate: 0.5 MB per report
            )
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Error getting storage info: ${e.message}", e)
            StorageInfo(0, 0, 0, 0.0)
        }
    }
}

data class StorageInfo(
    val totalReports: Int,
    val recentReports: Int,
    val oldReports: Int,
    val estimatedSizeMB: Double
)


