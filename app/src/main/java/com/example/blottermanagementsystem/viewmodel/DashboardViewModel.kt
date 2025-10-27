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
                // TEMPORARY: Load from local first, then sync from cloud
                Log.d("DashboardViewModel", "📱 Loading dashboard stats from local database first...")
                loadDashboardStatsFromLocal()
                
                // Then try to sync from cloud in background
                Log.d("DashboardViewModel", "📊 Syncing dashboard stats from cloud API...")
                val result = apiRepository.getDashboardAnalytics()
                
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
                        _dashboardStats.value = DashboardStats(
                            totalReports = data.totalReports,
                            pendingReports = data.pendingReports,
                            ongoingReports = data.ongoingReports,
                            resolvedReports = data.resolvedReports,
                            archivedReports = data.archivedReports,
                            totalOfficers = data.totalOfficers,
                            totalUsers = data.totalUsers
                        )
                        Log.d("DashboardViewModel", "✅ Dashboard stats synced from cloud: Users=${data.totalUsers}, Reports=${data.totalReports}")
                    }
                } else {
                    Log.e("DashboardViewModel", "❌ Failed to sync stats from cloud, using local data")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "❌ Error syncing stats from cloud: ${e.message}, using local data", e)
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
            Log.d("DashboardViewModel", "✅ Local stats loaded: Users=$totalUsers, Reports=$totalReports")
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
        
        // Notify assigned officers
        try {
            val report = repository.getReportById(hearing.blotterReportId)
            if (report != null && report.assignedOfficerIds.isNotEmpty()) {
                val officerIds = report.assignedOfficerIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                officerIds.forEach { officerId ->
                    val officerNotification = com.example.blottermanagementsystem.data.entity.Notification(
                        userId = officerId,
                        title = "Hearing Scheduled",
                        message = "Hearing scheduled for case $caseNumber on ${hearing.hearingDate} at ${hearing.hearingTime}",
                        type = "HEARING_SCHEDULED",
                        caseId = hearing.blotterReportId,
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertNotification(officerNotification)
                }
                Log.d("DashboardViewModel", "✅ Officers notified about hearing")
            }
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "❌ Failed to notify officers about hearing: ${e.message}", e)
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
}


