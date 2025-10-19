package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.User
import com.example.blottermanagementsystem.data.entity.ActivityLog
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.SecurityUtils
import com.example.blottermanagementsystem.data.api.ApiRepository
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
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
    
    private val preferencesManager = PreferencesManager(application)
    private val apiRepository = ApiRepository()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Try API login first
                Log.d("AuthViewModel", "Attempting API login for: $username")
                val apiResult = apiRepository.login(username, password)
                
                apiResult.onSuccess { apiUser ->
                    Log.d("AuthViewModel", "✅ API Login Success: ${apiUser.firstName} ${apiUser.lastName}")
                    
                    // Save user session from API response
                    preferencesManager.saveUserSession(
                        userId = apiUser.id.hashCode(), // Convert UUID string to Int
                        username = apiUser.username,
                        role = apiUser.role,
                        firstName = apiUser.firstName,
                        lastName = apiUser.lastName,
                        profilePhoto = apiUser.profilePicture
                    )
                    
                    // Role-based profile picture logic
                    when (apiUser.role) {
                        "Admin", "Officer" -> {
                            preferencesManager.hasSelectedProfilePicture = true
                        }
                        else -> {
                            preferencesManager.hasSelectedProfilePicture = true // Set to true for now
                        }
                    }
                    
                    // Create User entity for AuthState
                    val user = User(
                        id = apiUser.id.hashCode(),
                        firstName = apiUser.firstName,
                        lastName = apiUser.lastName,
                        username = apiUser.username,
                        password = "", // Don't store password
                        role = apiUser.role,
                        profileCompleted = true
                    )
                    
                    _authState.value = AuthState.Success(user)
                    return@launch
                }
                
                // If API login fails, try Room database as fallback
                Log.w("AuthViewModel", "API login failed, trying Room database")
                val user = repository.getUserByUsername(username)
                
                if (user == null) {
                    _authState.value = AuthState.Error("User not found")
                    return@launch
                }
                
                if (!SecurityUtils.verifyPassword(password, user.password)) {
                    _authState.value = AuthState.Error("Incorrect username or password")
                    return@launch
                }
                
                // Auto-detect role based on username prefix
                val actualRole = when {
                    username.startsWith("off.", ignoreCase = true) -> "Officer"
                    else -> user.role
                }
                
                // Save user session
                preferencesManager.saveUserSession(
                    userId = user.id,
                    username = user.username,
                    role = actualRole,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    profilePhoto = user.profilePhotoUri
                )
                
                // Role-based profile picture logic
                when (actualRole) {
                    "Admin", "Officer" -> {
                        preferencesManager.hasSelectedProfilePicture = true
                    }
                    else -> {
                        if (!user.profileCompleted) {
                            preferencesManager.hasSelectedProfilePicture = false
                        }
                    }
                }
                
                _authState.value = AuthState.Success(user.copy(role = actualRole))
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error: ${e.message}", e)
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }
    
    fun register(
        firstName: String,
        lastName: String,
        username: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Check if username already exists
                val existingUser = repository.getUserByUsername(username)
                if (existingUser != null) {
                    _authState.value = AuthState.Error("Username already exists")
                    return@launch
                }
                
                // Hash password
                val hashedPassword = SecurityUtils.hashPassword(password)
                
                // Create new user
                val newUser = User(
                    firstName = firstName,
                    lastName = lastName,
                    username = username,
                    password = hashedPassword,
                    role = role,
                    profileCompleted = false
                )
                
                val userId = repository.insertUser(newUser)
                
                // Don't auto-login after registration
                // User must login manually, then select profile picture
                // Make sure profile selection flag is reset for new users
                preferencesManager.hasSelectedProfilePicture = false
                
                _authState.value = AuthState.Success(newUser.copy(id = userId.toInt()))
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    fun logout() {
        preferencesManager.clearSession()
        _authState.value = AuthState.Idle
    }
    
    fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn
    }
    
    fun getCurrentUserRole(): String? {
        return preferencesManager.userRole
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    
    suspend fun changePassword(userId: Int, currentPassword: String, newPassword: String): Boolean {
        return try {
            val user = repository.getUserById(userId) ?: return false
            
            // Verify current password
            if (!SecurityUtils.verifyPassword(currentPassword, user.password)) {
                return false
            }
            
            // Hash new password
            val hashedNewPassword = SecurityUtils.hashPassword(newPassword)
            
            // Update user password
            val updatedUser = user.copy(password = hashedNewPassword)
            repository.updateUser(updatedUser)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateUserProfile(userId: Int, firstName: String, lastName: String, username: String): Boolean {
        return try {
            val user = repository.getUserById(userId) ?: return false
            
            // Check if username is already taken by another user
            val existingUser = repository.getUserByUsername(username)
            if (existingUser != null && existingUser.id != userId) {
                return false // Username already taken
            }
            
            // Update user profile
            val updatedUser = user.copy(
                firstName = firstName,
                lastName = lastName,
                username = username
            )
            repository.updateUser(updatedUser)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteUserAccount(userId: Int): Boolean {
        return try {
            val user = repository.getUserById(userId) ?: return false
            
            // Get all reports created by this user
            repository.getReportsByUser(userId).collect { userReports ->
                // Delete all related data for each report
                userReports.forEach { report ->
                    // Delete witnesses, suspects, evidence, hearings, status history
                    repository.deleteWitnessesByReportId(report.id)
                    repository.deleteSuspectsByReportId(report.id)
                    repository.deleteEvidenceByReportId(report.id)
                    repository.deleteHearingsByReportId(report.id)
                    repository.deleteStatusHistoryByReportId(report.id)
                    
                    // Delete the report itself
                    repository.deleteReport(report)
                }
            }
            
            // Delete all user notifications
            repository.deleteAllNotificationsByUserId(userId)
            
            // Finally, delete the user
            repository.deleteUser(user)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateUserCredentials(userId: Int, newUsername: String, newPassword: String): Boolean {
        return try {
            val user = repository.getUserById(userId) ?: return false
            
            // Check if new username is already taken by another user
            val existingUser = repository.getUserByUsername(newUsername)
            if (existingUser != null && existingUser.id != userId) {
                return false // Username already taken
            }
            
            // Hash new password
            val hashedPassword = SecurityUtils.hashPassword(newPassword)
            
            // Update user credentials and mark profile as completed
            val updatedUser = user.copy(
                username = newUsername,
                password = hashedPassword,
                profileCompleted = true,
                mustChangePassword = false // Clear the flag after password change
            )
            repository.updateUser(updatedUser)
            
            // Update session with new username
            preferencesManager.saveUserSession(
                userId = user.id,
                username = newUsername,
                role = user.role,
                firstName = user.firstName,
                lastName = user.lastName,
                profilePhoto = user.profilePhotoUri
            )
            
            // Clear the mustChangePassword flag in preferences
            preferencesManager.mustChangePassword = false
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun markProfileCompleted(userId: Int) {
        try {
            val user = repository.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(profileCompleted = true)
                repository.updateUser(updatedUser)
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    // Helper function to log activities
    private fun logActivity(
        activityType: String,
        description: String,
        performedBy: String
    ) {
        viewModelScope.launch {
            try {
                val activityLog = ActivityLog(
                    activityType = activityType,
                    description = description,
                    performedBy = performedBy,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertActivityLog(activityLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
