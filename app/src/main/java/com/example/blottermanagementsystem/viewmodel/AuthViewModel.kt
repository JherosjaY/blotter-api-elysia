package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.User
import com.example.blottermanagementsystem.data.entity.ActivityLog
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import com.example.blottermanagementsystem.data.repository.ApiRepository
import com.example.blottermanagementsystem.utils.FCMHelper
import com.example.blottermanagementsystem.utils.PreferencesManager
import com.example.blottermanagementsystem.utils.SecurityUtils
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
        mediationSessionDao = database.mediationSessionDao(),
        caseTimelineDao = database.caseTimelineDao(),
        caseTemplateDao = database.caseTemplateDao()
    )
    
    private val preferencesManager = PreferencesManager(application)
    private val apiRepository = ApiRepository()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // ☁️ CLOUD FIRST: Try API login
                Log.d("AuthViewModel", "🌐 Attempting cloud API login for: $username")
                val apiResult = apiRepository.login(username, password)
                
                apiResult.onSuccess { apiUser ->
                    Log.d("AuthViewModel", "✅ Cloud Login Success: ${apiUser.firstName} ${apiUser.lastName}")
                    
                    // Save/update user in local database for offline access
                    val existingUser = repository.getUserByUsername(username)
                    if (existingUser != null) {
                        // Update existing user
                        repository.updateUser(apiUser.copy(id = existingUser.id))
                    } else {
                        // Insert new user
                        repository.insertUser(apiUser)
                    }
                    
                    // Save session with actual role from database
                    preferencesManager.saveUserSession(
                        userId = apiUser.id,
                        username = apiUser.username,
                        role = apiUser.role,
                        firstName = apiUser.firstName,
                        lastName = apiUser.lastName,
                        profilePhoto = apiUser.profilePhotoUri
                    )
                    
                    // Profile picture logic - check if user already completed profile
                    when (apiUser.role) {
                        "Admin", "Officer" -> {
                            preferencesManager.hasSelectedProfilePicture = true
                        }
                        else -> {
                            // If user already completed profile (has photo or profileCompleted=true), skip selection
                            val hasProfile = !apiUser.profilePhotoUri.isNullOrEmpty() || apiUser.profileCompleted
                            preferencesManager.hasSelectedProfilePicture = hasProfile
                        }
                    }
                    
                    // Subscribe to FCM topics based on role
                    FCMHelper.subscribeToTopics(apiUser.role)
                    
                    // Update FCM token for this device
                    updateFcmTokenOnLogin(apiUser.id)
                    
                    _authState.value = AuthState.Success(apiUser)
                    return@launch
                }
                
                // ⚠️ API FAILED: Fallback to local database
                apiResult.onFailure { apiError ->
                    Log.w("AuthViewModel", "⚠️ Cloud login failed: ${apiError.message}")
                    Log.d("AuthViewModel", "📱 Falling back to local database...")
                    
                    val localUser = repository.getUserByUsername(username)
                    
                    if (localUser != null) {
                        // Verify password
                        val passwordMatches = if (SecurityUtils.isPasswordHashed(localUser.password)) {
                            SecurityUtils.verifyPassword(password, localUser.password)
                        } else {
                            localUser.password == password
                        }
                        
                        if (passwordMatches) {
                            Log.d("AuthViewModel", "✅ Local Login Success: ${localUser.firstName} ${localUser.lastName}")
                            
                            // ☁️ SYNC ADMIN TO CLOUD: If admin logs in locally, try to sync to cloud
                            if (localUser.role == "Admin" && localUser.username == "admin") {
                                try {
                                    Log.d("AuthViewModel", "☁️ Syncing admin to cloud...")
                                    val syncResult = apiRepository.register(
                                        username = localUser.username,
                                        password = password, // Use plain password for registration
                                        firstName = localUser.firstName,
                                        lastName = localUser.lastName,
                                        role = "Admin"
                                    )
                                    if (syncResult.isSuccess) {
                                        Log.d("AuthViewModel", "✅ Admin synced to cloud successfully")
                                    }
                                } catch (e: Exception) {
                                    Log.w("AuthViewModel", "⚠️ Admin cloud sync failed (may already exist): ${e.message}")
                                    // Continue with local login even if sync fails
                                }
                            }
                            
                            // Use actual role from database, no auto-detection
                            preferencesManager.saveUserSession(
                                userId = localUser.id,
                                username = localUser.username,
                                role = localUser.role,
                                firstName = localUser.firstName,
                                lastName = localUser.lastName,
                                profilePhoto = localUser.profilePhotoUri
                            )
                            
                            when (localUser.role) {
                                "Admin", "Officer" -> {
                                    preferencesManager.hasSelectedProfilePicture = true
                                }
                                else -> {
                                    // If user already completed profile, skip selection
                                    val hasProfile = !localUser.profilePhotoUri.isNullOrEmpty() || localUser.profileCompleted
                                    preferencesManager.hasSelectedProfilePicture = hasProfile
                                }
                            }
                            
                            // Subscribe to FCM topics based on role
                            FCMHelper.subscribeToTopics(localUser.role)
                            
                            // Update FCM token for this device
                            updateFcmTokenOnLogin(localUser.id)
                            
                            _authState.value = AuthState.Success(localUser)
                            return@launch
                        } else {
                            _authState.value = AuthState.Error("Incorrect username or password")
                            return@launch
                        }
                    } else {
                        _authState.value = AuthState.Error("User not found. Please check your internet connection.")
                        return@launch
                    }
                }
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
                // ☁️ CLOUD FIRST: Try API registration
                Log.d("AuthViewModel", "🌐 Attempting cloud API registration for: $username")
                val apiResult = apiRepository.register(username, password, firstName, lastName, role)
                
                apiResult.onSuccess { apiUser ->
                    Log.d("AuthViewModel", "✅ Cloud Registration Success: ${apiUser.firstName} ${apiUser.lastName}")
                    
                    // Save to local database
                    repository.insertUser(apiUser)
                    
                    // Update FCM token for this device (so user can receive notifications immediately)
                    updateFcmTokenOnLogin(apiUser.id)
                    
                    preferencesManager.hasSelectedProfilePicture = false
                    _authState.value = AuthState.Success(apiUser)
                    return@launch
                }
                
                // ⚠️ API FAILED: Fallback to local registration
                apiResult.onFailure { apiError ->
                    Log.w("AuthViewModel", "⚠️ Cloud registration failed: ${apiError.message}")
                    Log.d("AuthViewModel", "📱 Falling back to local registration...")
                    
                    // Check if username already exists locally
                    val existingUser = repository.getUserByUsername(username)
                    if (existingUser != null) {
                        _authState.value = AuthState.Error("Username already exists")
                        return@launch
                    }
                    
                    // Hash password
                    val hashedPassword = SecurityUtils.hashPassword(password)
                    
                    // Save to local Room database
                    Log.d("AuthViewModel", "Registering user locally...")
                    val newUser = User(
                        firstName = firstName,
                        lastName = lastName,
                        username = username,
                        password = hashedPassword,
                        role = role,
                        profileCompleted = false
                    )
                    
                    val userId = repository.insertUser(newUser)
                    
                    // Update FCM token for this device (so user can receive notifications immediately)
                    updateFcmTokenOnLogin(userId.toInt())
                    
                    preferencesManager.hasSelectedProfilePicture = false
                    _authState.value = AuthState.Success(newUser.copy(id = userId.toInt()))
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error", e)
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    fun logout() {
        // Unsubscribe from FCM topics
        FCMHelper.unsubscribeFromAllTopics()
        
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
            
            // Update user password locally
            val updatedUser = user.copy(password = hashedNewPassword)
            repository.updateUser(updatedUser)
            
            // Sync to cloud API
            try {
                val result = apiRepository.updateUserPassword(userId, hashedNewPassword)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "✅ Password synced to cloud successfully")
                } else {
                    Log.e("AuthViewModel", "⚠️ Failed to sync password to cloud")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Error syncing password to cloud: ${e.message}", e)
            }
            
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
            
            // Update user profile locally
            val updatedUser = user.copy(
                firstName = firstName,
                lastName = lastName,
                username = username
            )
            repository.updateUser(updatedUser)
            
            // Sync to cloud API
            try {
                val result = apiRepository.updateUserInfo(userId, firstName, lastName, username)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "✅ Profile synced to cloud successfully")
                } else {
                    Log.e("AuthViewModel", "⚠️ Failed to sync profile to cloud")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Error syncing profile to cloud: ${e.message}", e)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteUserAccount(userId: Int): Boolean {
        return try {
            val user = repository.getUserById(userId) ?: return false
            
            // ⚡ Try API first (delete from cloud)
            try {
                Log.d("AuthViewModel", "🌐 Deleting user account from API...")
                val apiResult = apiRepository.deleteUser(userId)
                
                apiResult.onSuccess {
                    Log.d("AuthViewModel", "✅ User deleted from cloud successfully")
                }.onFailure { error ->
                    Log.w("AuthViewModel", "⚠️ API deletion failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "⚠️ API deletion error: ${e.message}")
            }
            
            // Get all reports created by this user
            val userReports = repository.getReportsByUser(userId).first()
            
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
            
            // Delete all user notifications
            repository.deleteAllNotificationsByUserId(userId)
            
            // Finally, delete the user from local database
            repository.deleteUser(user)
            
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Delete account error", e)
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
                
                // Update local database
                repository.updateUser(updatedUser)
                
                // TODO: Sync to cloud
                // apiRepository.updateUserProfile(userId, profileCompleted = true)
                
                Log.d("AuthViewModel", "✅ Profile marked as completed for user $userId")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Error marking profile completed: ${e.message}", e)
        }
    }
    
    suspend fun updateUserProfile(userId: Int, profilePhotoUri: String) {
        try {
            val user = repository.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    profilePhotoUri = profilePhotoUri,
                    profileCompleted = true
                )
                
                // Update local database first
                repository.updateUser(updatedUser)
                Log.d("AuthViewModel", "✅ Profile updated locally for user $userId")
                
                // Sync to cloud
                try {
                    val result = apiRepository.updateUserProfile(userId, profilePhotoUri)
                    if (result.isSuccess) {
                        Log.d("AuthViewModel", "✅ Profile synced to cloud successfully")
                    } else {
                        Log.w("AuthViewModel", "⚠️ Failed to sync profile to cloud: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.w("AuthViewModel", "⚠️ Cloud sync failed (will retry later): ${e.message}")
                    // Continue even if cloud sync fails - profile is saved locally
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Error updating profile: ${e.message}", e)
        }
    }
    
    // Get user by ID (with cloud sync)
    suspend fun getUserById(userId: Int): User? {
        return try {
            // Try to sync from cloud first
            try {
                Log.d("AuthViewModel", "🔄 Syncing user $userId from cloud...")
                val result = apiRepository.getUserFromCloud(userId)
                if (result.isSuccess) {
                    val cloudUser = result.getOrNull()
                    if (cloudUser != null) {
                        // Update local database with cloud data
                        repository.insertUser(cloudUser)
                        Log.d("AuthViewModel", "✅ User synced from cloud")
                        return cloudUser
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "⚠️ Cloud sync failed, using local data: ${e.message}")
            }
            
            // Fallback to local database
            repository.getUserById(userId)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Error getting user: ${e.message}", e)
            null
        }
    }
    
    // Get all users (for admin notification sender)
    suspend fun getAllUsersSync(): List<User> {
        return try {
            repository.getAllUsersSync()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ Error getting all users: ${e.message}", e)
            emptyList()
        }
    }
    
    // Update FCM token on login for multi-device support
    private fun updateFcmTokenOnLogin(userId: Int) {
        viewModelScope.launch {
            try {
                // Get current FCM token from Firebase
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        viewModelScope.launch {
                            try {
                                Log.d("AuthViewModel", "🔥 Updating FCM token for user $userId on this device")
                                val deviceId = android.provider.Settings.Secure.getString(
                                    getApplication<Application>().contentResolver,
                                    android.provider.Settings.Secure.ANDROID_ID
                                )
                                
                                // Update token in cloud
                                val result = apiRepository.updateFcmToken(userId, token, deviceId)
                                if (result.isSuccess) {
                                    Log.d("AuthViewModel", "✅ FCM token updated successfully for multi-device support")
                                } else {
                                    Log.w("AuthViewModel", "⚠️ Failed to update FCM token: ${result.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "❌ Error updating FCM token: ${e.message}", e)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthViewModel", "❌ Failed to get FCM token: ${e.message}", e)
                    }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "❌ Error in updateFcmTokenOnLogin: ${e.message}", e)
            }
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

