package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.User
import com.example.blottermanagementsystem.data.entity.Officer
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.data.entity.Notification
import com.example.blottermanagementsystem.data.entity.ActivityLog
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {
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
    
    // User Management
    val allUsers: Flow<List<User>> = repository.getAllUsers()
    
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
    
    // Officer Management
    private val _allOfficers = MutableStateFlow<List<Officer>>(emptyList())
    val allOfficers: StateFlow<List<Officer>> = _allOfficers
    
    init {
        loadOfficers()
    }
    
    private fun loadOfficers() {
        viewModelScope.launch {
            try {
                // Load officers from database
                repository.getAllOfficersSync()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading officers: ${e.message}")
            }
        }
    }
    
    suspend fun addOfficer(officer: Officer) {
        // Save to local Room database
        repository.insertOfficer(officer)
    }
    
    suspend fun createOfficerAccount(
        firstName: String,
        lastName: String,
        username: String,
        tempPassword: String,
        badgeNumber: String
    ): Int {
        // Hash the temporary password
        val hashedPassword = com.example.blottermanagementsystem.utils.SecurityUtils.hashPassword(tempPassword)
        
        val newUser = com.example.blottermanagementsystem.data.entity.User(
            firstName = firstName,
            lastName = lastName,
            username = username,
            password = hashedPassword,
            role = "Officer",
            profileCompleted = false, // Force profile setup on first login
            mustChangePassword = true, // Force password change on first login
            badgeNumber = badgeNumber,
            isActive = true
        )
        
        val userId = repository.insertUser(newUser).toInt()
        return userId
    }
    
    suspend fun updateOfficer(officer: Officer) {
        repository.updateOfficer(officer)
    }
    
    suspend fun deleteOfficer(officerId: Int) {
        val officer = repository.getOfficerById(officerId)
        officer?.let {
            repository.deleteOfficer(it)
        }
    }
    
    suspend fun assignOfficerToReport(reportId: Int, officerId: Int, assignedBy: String = "Admin") {
        val report = repository.getReportById(reportId)
        report?.let {
            // Update report with assigned officer
            val updatedReport = it.copy(
                assignedOfficerId = officerId,
                assignedOfficerIds = officerId.toString(), // Set the officer ID for filtering
                status = "Assigned" // Officer will change to "Under Investigation" when they start
            )
            repository.updateReport(updatedReport)
            
            // Increment officer's assigned cases count
            val officer = repository.getOfficerById(officerId)
            officer?.let { off ->
                val updatedOfficer = off.copy(assignedCases = off.assignedCases + 1)
                repository.updateOfficer(updatedOfficer)
                
                // Notify the officer
                try {
                    val notification = Notification(
                        userId = off.userId ?: 0,
                        title = "New Case Assigned",
                        message = "You have been assigned to case ${it.caseNumber}",
                        type = "CASE_ASSIGNED",
                        caseId = reportId,
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertNotification(notification)
                    
                    // Log activity
                    val activityLog = ActivityLog(
                        caseId = reportId,
                        caseTitle = it.caseNumber,
                        activityType = "Officer Assigned",
                        description = "Officer ${off.name} assigned to case",
                        performedBy = assignedBy,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertActivityLog(activityLog)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    // Report Management
    val allReports: Flow<List<BlotterReport>> = repository.getAllActiveReports()
    
    fun getReportsByStatus(status: String): Flow<List<BlotterReport>> {
        return repository.getReportsByStatus(status)
    }
    
    suspend fun updateReportStatus(reportId: Int, newStatus: String, notes: String = "", userId: Int = 0) {
        val report = repository.getReportById(reportId)
        report?.let {
            // Save status history
            if (it.status != newStatus) {
                val statusHistory = com.example.blottermanagementsystem.data.entity.StatusHistory(
                    blotterReportId = reportId,
                    previousStatus = it.status,
                    newStatus = newStatus,
                    remarks = notes.ifBlank { null },
                    changedBy = userId
                )
                repository.insertStatusHistory(statusHistory)
            }
            
            // Update report status
            val updatedReport = it.copy(status = newStatus)
            repository.updateReport(updatedReport)
        }
    }
    
    suspend fun archiveReport(reportId: Int) {
        val report = repository.getReportById(reportId)
        report?.let {
            val updatedReport = it.copy(isArchived = true)
            repository.updateReport(updatedReport)
        }
    }
    
    // Case Assignment
    suspend fun assignOfficersToCase(reportId: Int, officerIds: List<Int>, assignedBy: String = "Admin") {
        val report = repository.getReportById(reportId)
        report?.let {
            // Get previously assigned officers
            val previousOfficerIds = it.assignedOfficerIds
                .split(",")
                .filter { id -> id.isNotBlank() }
                .mapNotNull { id -> id.toIntOrNull() }
            
            // Decrement count for officers being removed
            val removedOfficers = previousOfficerIds.filter { id -> !officerIds.contains(id) }
            removedOfficers.forEach { officerId ->
                val officer = repository.getOfficerById(officerId)
                officer?.let { off ->
                    val newCount = (off.assignedCases - 1).coerceAtLeast(0)
                    val updatedOfficer = off.copy(assignedCases = newCount)
                    repository.updateOfficer(updatedOfficer)
                }
            }
            
            // Increment count for new officers
            val newOfficers = officerIds.filter { id -> !previousOfficerIds.contains(id) }
            newOfficers.forEach { officerId ->
                val officer = repository.getOfficerById(officerId)
                officer?.let { off ->
                    val updatedOfficer = off.copy(assignedCases = off.assignedCases + 1)
                    repository.updateOfficer(updatedOfficer)
                    
                    // Notify each newly assigned officer
                    try {
                        val notification = Notification(
                            userId = off.userId ?: 0,
                            title = "New Case Assigned",
                            message = "You have been assigned to case ${it.caseNumber}",
                            type = "CASE_ASSIGNED",
                            caseId = reportId,
                            isRead = false,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.insertNotification(notification)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            // Log activity if officers were added
            if (newOfficers.isNotEmpty()) {
                try {
                    val activityLog = ActivityLog(
                        caseId = reportId,
                        caseTitle = it.caseNumber,
                        activityType = "Officers Assigned",
                        description = "${newOfficers.size} officer(s) assigned to case",
                        performedBy = assignedBy,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertActivityLog(activityLog)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Store as comma-separated IDs
            val assignedIds = officerIds.joinToString(",")
            val updatedReport = it.copy(
                assignedOfficerIds = assignedIds,
                status = if (it.status == "Pending") "Under Investigation" else it.status
            )
            repository.updateReport(updatedReport)
        }
    }
    
    suspend fun getAssignedOfficerIds(reportId: Int): List<Int> {
        val report = repository.getReportById(reportId)
        return report?.assignedOfficerIds
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toIntOrNull() }
            ?: emptyList()
    }
    
    // Statistics
    suspend fun getSystemStatistics(): SystemStatistics {
        val users = repository.getAllUsersSync()
        val officers = repository.getAllOfficersSync()
        val reports = repository.getAllReportsSync()
        
        return SystemStatistics(
            totalUsers = users.count { it.isActive && it.role != "Admin" },
            activeUsers = users.count { it.isActive && it.role != "Admin" },
            totalOfficers = officers.count { it.isAvailable },
            totalReports = reports.count { !it.isArchived },
            pendingReports = reports.count { it.status == "Pending" && !it.isArchived },
            ongoingReports = reports.count { it.status == "Under Investigation" && !it.isArchived },
            resolvedReports = reports.count { it.status == "Resolved" && !it.isArchived },
            archivedReports = reports.count { it.isArchived }
        )
    }
}

data class SystemStatistics(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalOfficers: Int,
    val totalReports: Int,
    val pendingReports: Int,
    val ongoingReports: Int,
    val resolvedReports: Int,
    val archivedReports: Int
)

