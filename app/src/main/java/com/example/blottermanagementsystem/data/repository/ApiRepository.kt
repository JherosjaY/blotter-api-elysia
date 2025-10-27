package com.example.blottermanagementsystem.data.repository

import android.util.Log
import com.example.blottermanagementsystem.data.api.ApiConfig
import com.example.blottermanagementsystem.data.api.DashboardAnalyticsData
import com.example.blottermanagementsystem.data.api.FcmTokenRequest
import com.example.blottermanagementsystem.data.api.LoginRequest
import com.example.blottermanagementsystem.data.api.RegisterRequest
import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * API Repository - Handles all API calls with error handling
 */
class ApiRepository {
    
    private val apiService = ApiConfig.apiService
    private val TAG = "ApiRepository"
    
    // ==================== Authentication ====================
    
    suspend fun login(username: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 API Login - Calling: https://blotter-backend.onrender.com/api/auth/login")
            Log.d(TAG, "🌐 API Login - Username: $username")
            val response = apiService.login(LoginRequest(username, password))
            
            Log.d(TAG, "🌐 API Response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            Log.d(TAG, "🌐 API Response - Body: ${response.body()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()?.data
                if (loginData != null && loginData.user != null) {
                    // Convert LoginData to User model
                    // Handle nullable fields from API with defaults
                    val userData = loginData.user
                    
                    // If user has a profile photo URI, consider profile completed
                    val hasProfilePhoto = !userData.profilePhotoUri.isNullOrEmpty()
                    val isProfileCompleted = if (hasProfilePhoto) {
                        true // User has profile photo, so profile is completed
                    } else {
                        userData.profileCompleted ?: false
                    }
                    
                    val user = User(
                        id = userData.id,
                        firstName = userData.firstName ?: "User",
                        lastName = userData.lastName ?: "",
                        username = userData.username,
                        password = "", // Don't store password from API
                        role = userData.role,
                        profilePhotoUri = userData.profilePhotoUri ?: "",
                        profileCompleted = isProfileCompleted
                    )
                    Log.d(TAG, "✅ User created: ${user.firstName} ${user.lastName} (${user.role})")
                    Result.success(user)
                } else {
                    Result.failure(Exception("Login data is null"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Login failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun register(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 API Register - Calling: https://blotter-backend.onrender.com/api/auth/register")
            Log.d(TAG, "🌐 API Register - Username: $username, Role: $role")
            val request = RegisterRequest(username, password, firstName, lastName, role)
            val response = apiService.register(request)
            
            Log.d(TAG, "🌐 API Response - Code: ${response.code()}, Success: ${response.isSuccessful}")
            Log.d(TAG, "🌐 API Response - Body: ${response.body()}")
            Log.d(TAG, "🌐 API Response - Error: ${response.errorBody()?.string()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()?.data
                if (loginData != null && loginData.user != null) {
                    // Handle nullable fields from API with defaults
                    val userData = loginData.user
                    
                    // If user has a profile photo URI, consider profile completed
                    val hasProfilePhoto = !userData.profilePhotoUri.isNullOrEmpty()
                    val isProfileCompleted = if (hasProfilePhoto) {
                        true // User has profile photo, so profile is completed
                    } else {
                        userData.profileCompleted ?: false
                    }
                    
                    val user = User(
                        id = userData.id,
                        firstName = userData.firstName ?: firstName,
                        lastName = userData.lastName ?: lastName,
                        username = userData.username,
                        password = "",
                        role = userData.role,
                        profilePhotoUri = userData.profilePhotoUri ?: "",
                        profileCompleted = isProfileCompleted
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception("Registration data is null"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Registration failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Register error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Users ====================
    
    suspend fun deleteUser(userId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteUser(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val message = response.body()?.message ?: "User deleted successfully"
                Result.success(message)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to delete user"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Delete user error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Blotter Reports ====================
    
    suspend fun getAllReports(): Result<List<BlotterReport>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllReports()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val reportsData = response.body()?.data ?: emptyList()
                val reports = reportsData.map { reportData ->
                    BlotterReport(
                        id = reportData.id,
                        userId = reportData.userId,
                        caseNumber = reportData.caseNumber ?: "CASE-${reportData.id}",
                        complainantName = "",
                        complainantContact = "",
                        complainantAddress = "",
                        incidentType = reportData.incidentType,
                        incidentDate = reportData.dateFiled,
                        incidentTime = "",
                        incidentLocation = reportData.incidentLocation,
                        narrative = reportData.narrative,
                        status = reportData.status,
                        dateFiled = reportData.dateFiled,
                        assignedOfficer = "",
                        assignedOfficerId = reportData.officerId,
                        assignedOfficerIds = reportData.officerId?.toString() ?: "",
                        isArchived = false,
                        audioUri = reportData.audioUri ?: "" // ✅ Audio synced from cloud!
                    )
                }
                Result.success(reports)
            } else {
                Result.failure(Exception("Failed to fetch reports"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get all reports error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createReport(report: BlotterReport): Result<BlotterReport> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 API Create Report - Calling: ${report.caseNumber}")
            val response = apiService.createReport(report)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val createdReport = response.body()?.data
                if (createdReport != null) {
                    Log.d(TAG, "✅ Report created successfully: ${createdReport.id}")
                    return@withContext Result.success(createdReport)
                } else {
                    Log.e(TAG, "❌ Created report is null")
                    return@withContext Result.failure(Exception("Created report is null"))
                }
            } else {
                Log.e(TAG, "❌ Failed to create report: ${response.code()}")
                return@withContext Result.failure(Exception("Failed to create report: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Create report error: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    suspend fun updateReport(id: Int, report: BlotterReport): Result<BlotterReport> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🌐 API Update Report - ID: $id")
            val response = apiService.updateReport(id, report)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedReport = response.body()?.data
                if (updatedReport != null) {
                    Log.d(TAG, "✅ Report updated successfully: $id")
                    return@withContext Result.success(updatedReport)
                } else {
                    Log.e(TAG, "❌ Updated report is null")
                    return@withContext Result.failure(Exception("Updated report is null"))
                }
            } else {
                Log.e(TAG, "❌ Failed to update report: ${response.code()}")
                return@withContext Result.failure(Exception("Failed to update report: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update report error: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }
    
    // ==================== FCM Token ====================
    
    suspend fun updateFcmToken(userId: Int, fcmToken: String, deviceId: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔥 Sending FCM token to backend for user $userId")
            val request = FcmTokenRequest(userId, fcmToken, deviceId)
            val response = apiService.updateFcmToken(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ FCM token sent successfully")
                Result.success(true)
            } else {
                Log.e(TAG, "❌ Failed to send FCM token: ${response.body()?.message}")
                Result.failure(Exception("Failed to send FCM token"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ FCM token error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Update User Profile (Profile Photo)
    suspend fun updateUserProfile(userId: Int, profilePhotoUri: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📸 Updating user profile for user $userId")
            val request = com.example.blottermanagementsystem.data.api.UpdateProfileRequest(
                profilePhotoUri = profilePhotoUri,
                profileCompleted = true
            )
            val response = apiService.updateUserProfile(userId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    val user = User(
                        id = userData.id,
                        firstName = userData.firstName ?: "User",
                        lastName = userData.lastName ?: "",
                        username = userData.username,
                        password = "",
                        role = userData.role,
                        profilePhotoUri = userData.profilePhotoUri,
                        profileCompleted = true
                    )
                    Log.d(TAG, "✅ Profile updated successfully")
                    return@withContext Result.success(user)
                }
            }
            Log.e(TAG, "❌ Failed to update profile: ${response.body()?.message}")
            Result.failure(Exception("Failed to update profile"))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Profile update error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Evidence ====================
    
    suspend fun createEvidence(evidence: com.example.blottermanagementsystem.data.entity.Evidence): Result<com.example.blottermanagementsystem.data.entity.Evidence> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📸 Creating evidence in cloud...")
            val response = apiService.createEvidence(evidence)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Evidence created in cloud: ${data.id}")
                    Result.success(data)
                } else {
                    Log.e(TAG, "❌ Evidence data is null")
                    Result.failure(Exception("No data"))
                }
            } else {
                Log.e(TAG, "❌ Failed to create evidence: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Evidence creation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getEvidenceByReportId(reportId: Int): Result<List<com.example.blottermanagementsystem.data.entity.Evidence>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📸 Fetching evidence for report $reportId from cloud...")
            val response = apiService.getEvidenceByReportId(reportId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                Log.d(TAG, "✅ Evidence fetched: ${data.size} items")
                Result.success(data)
            } else {
                Log.e(TAG, "❌ Failed to fetch evidence: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Evidence fetch error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteEvidence(evidenceId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Deleting evidence $evidenceId from cloud...")
            val response = apiService.deleteEvidence(evidenceId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ Evidence deleted from cloud")
                Result.success(true)
            } else {
                Log.e(TAG, "❌ Failed to delete evidence: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Evidence deletion error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Witnesses ====================
    
    suspend fun createWitness(witness: com.example.blottermanagementsystem.data.entity.Witness): Result<com.example.blottermanagementsystem.data.entity.Witness> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👥 Creating witness in cloud...")
            val response = apiService.createWitness(witness)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Witness created in cloud: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Witness creation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Suspects ====================
    
    suspend fun createSuspect(suspect: com.example.blottermanagementsystem.data.entity.Suspect): Result<com.example.blottermanagementsystem.data.entity.Suspect> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Creating suspect in cloud...")
            val response = apiService.createSuspect(suspect)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Suspect created in cloud: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Suspect creation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Resolutions ====================
    
    suspend fun createResolution(resolution: com.example.blottermanagementsystem.data.entity.Resolution): Result<com.example.blottermanagementsystem.data.entity.Resolution> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "✅ Creating resolution in cloud...")
            val response = apiService.createResolution(resolution)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Resolution created in cloud: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Resolution creation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Respondents ====================
    
    suspend fun createRespondent(respondent: com.example.blottermanagementsystem.data.entity.Respondent): Result<com.example.blottermanagementsystem.data.entity.Respondent> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📝 Creating respondent in cloud...")
            val response = apiService.createRespondent(respondent)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Respondent created in cloud: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Respondent creation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Activity Logs ====================
    
    suspend fun createActivityLog(activityLog: com.example.blottermanagementsystem.data.entity.ActivityLog): Result<com.example.blottermanagementsystem.data.entity.ActivityLog> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📋 Creating activity log in cloud...")
            val response = apiService.createActivityLog(activityLog)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Activity log created in cloud: ${data.id}")
                    Result.success(data)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Activity log creation error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== User Profile Updates ====================
    
    suspend fun updateUserPassword(userId: Int, hashedPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔐 Updating user password in cloud...")
            // Get current user data first
            val userResponse = apiService.getUserById(userId)
            if (!userResponse.isSuccessful || userResponse.body()?.data == null) {
                return@withContext Result.failure(Exception("User not found"))
            }
            
            val currentUser = userResponse.body()?.data!!
            val updatedUser = currentUser.copy(password = hashedPassword)
            
            val response = apiService.updateUser(userId, updatedUser)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ Password updated in cloud successfully")
                Result.success("Password updated")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update password"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Password update error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateUserInfo(userId: Int, firstName: String, lastName: String, username: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👤 Updating user info in cloud...")
            // Get current user data first
            val userResponse = apiService.getUserById(userId)
            if (!userResponse.isSuccessful || userResponse.body()?.data == null) {
                return@withContext Result.failure(Exception("User not found"))
            }
            
            val currentUser = userResponse.body()?.data!!
            val updatedUser = currentUser.copy(
                firstName = firstName,
                lastName = lastName,
                username = username
            )
            
            val response = apiService.updateUser(userId, updatedUser)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ User info updated in cloud successfully")
                Result.success("Profile updated")
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Profile update error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Dashboard ====================
    
    suspend fun getDashboardAnalytics(): Result<DashboardAnalyticsData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📊 Fetching dashboard analytics from cloud...")
            val response = apiService.getDashboardAnalytics()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Log.d(TAG, "✅ Dashboard analytics fetched: Users=${data.totalUsers}, Reports=${data.totalReports}")
                    Result.success(data)
                } else {
                    Log.e(TAG, "❌ Dashboard analytics data is null")
                    Result.failure(Exception("No data"))
                }
            } else {
                Log.e(TAG, "❌ Failed to fetch dashboard analytics: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Dashboard analytics error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Users ====================
    
    suspend fun getUserFromCloud(userId: Int): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👤 Fetching user $userId from cloud API...")
            val response = apiService.getUserById(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.data
                if (user != null) {
                    Log.d(TAG, "✅ Fetched user from cloud: ${user.username}")
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Log.e(TAG, "❌ Failed to fetch user: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching user from cloud: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAllUsersFromCloud(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👥 Fetching all users from cloud API...")
            val response = apiService.getAllUsers()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val usersData = response.body()?.data ?: emptyList()
                val users = usersData.map { userData ->
                    User(
                        id = userData.id,
                        firstName = userData.firstName ?: "User",
                        lastName = userData.lastName ?: "",
                        username = userData.username,
                        password = "", // Don't store password from API
                        role = userData.role,
                        profilePhotoUri = userData.profilePhotoUri ?: "",
                        profileCompleted = userData.profileCompleted ?: false,
                        isActive = userData.isActive ?: true,
                        mustChangePassword = userData.mustChangePassword ?: false,
                        fcmToken = userData.fcmToken,
                        deviceId = userData.deviceId
                    )
                }
                Log.d(TAG, "✅ Fetched ${users.size} users from cloud")
                Result.success(users)
            } else {
                Log.e(TAG, "❌ Failed to fetch users: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching users from cloud: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Officers ====================
    
    suspend fun getAllOfficersFromCloud(): Result<List<com.example.blottermanagementsystem.data.entity.Officer>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👮 Fetching all officers from cloud API...")
            val response = apiService.getAllOfficers()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val officersData = response.body()?.data ?: emptyList()
                val officers = officersData.map { officerData ->
                    com.example.blottermanagementsystem.data.entity.Officer(
                        id = officerData.id,
                        userId = officerData.userId,
                        name = officerData.name ?: "${officerData.badgeNumber}",
                        badgeNumber = officerData.badgeNumber ?: "",
                        rank = officerData.rank ?: "",
                        contactNumber = null,
                        email = null,
                        isActive = officerData.isActive ?: true
                    )
                }
                Log.d(TAG, "✅ Fetched ${officers.size} officers from cloud")
                Result.success(officers)
            } else {
                Log.e(TAG, "❌ Failed to fetch officers: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching officers from cloud: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer): Result<com.example.blottermanagementsystem.data.entity.Officer> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👮 Creating officer in cloud API...")
            val response = apiService.createOfficer(officer)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val createdOfficer = response.body()?.data
                if (createdOfficer != null) {
                    Log.d(TAG, "✅ Officer created in cloud: ${createdOfficer.name}")
                    Result.success(createdOfficer)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Log.e(TAG, "❌ Failed to create officer: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating officer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateOfficer(officer: com.example.blottermanagementsystem.data.entity.Officer): Result<com.example.blottermanagementsystem.data.entity.Officer> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👮 Updating officer in cloud API...")
            val response = apiService.updateOfficer(officer.id, officer)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedOfficer = response.body()?.data
                if (updatedOfficer != null) {
                    Log.d(TAG, "✅ Officer updated in cloud: ${updatedOfficer.name}")
                    Result.success(updatedOfficer)
                } else {
                    Result.failure(Exception("No data"))
                }
            } else {
                Log.e(TAG, "❌ Failed to update officer: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating officer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteOfficer(officerId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "👮 Deleting officer from cloud API...")
            val response = apiService.deleteOfficer(officerId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ Officer deleted from cloud")
                Result.success("Officer deleted")
            } else {
                Log.e(TAG, "❌ Failed to delete officer: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting officer: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Notifications ====================
    
    suspend fun syncNotificationsFromCloud(userId: Int): Result<List<com.example.blottermanagementsystem.data.entity.Notification>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔔 Syncing notifications from cloud for user $userId...")
            val response = apiService.getUserNotifications(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val notificationsData = response.body()?.data ?: emptyList()
                val notifications = notificationsData.map { data ->
                    com.example.blottermanagementsystem.data.entity.Notification(
                        id = data.id,
                        userId = data.userId,
                        title = data.title,
                        message = data.message,
                        type = data.type,
                        caseId = data.caseId,
                        isRead = data.isRead,
                        timestamp = data.timestamp
                    )
                }
                Log.d(TAG, "✅ Synced ${notifications.size} notifications from cloud")
                Result.success(notifications)
            } else {
                Result.failure(Exception("Failed to sync notifications"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing notifications: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun markNotificationAsReadInCloud(notificationId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📖 Marking notification $notificationId as read in cloud...")
            val response = apiService.markNotificationAsRead(notificationId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ Notification marked as read in cloud")
                Result.success("Marked as read")
            } else {
                Result.failure(Exception("Failed to mark as read"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error marking notification as read: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteNotificationInCloud(notificationId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🗑️ Deleting notification $notificationId from cloud...")
            val response = apiService.deleteNotification(notificationId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ Notification deleted from cloud")
                Result.success("Deleted")
            } else {
                Result.failure(Exception("Failed to delete"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting notification: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Reports ====================
    
    suspend fun getAllReportsFromCloud(): Result<List<BlotterReport>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📋 Fetching all reports from cloud API...")
            val response = apiService.getAllReports()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val reportsData = response.body()?.data ?: emptyList()
                val reports = reportsData.map { reportData ->
                    BlotterReport(
                        id = reportData.id,
                        userId = reportData.userId,
                        caseNumber = reportData.caseNumber ?: "CASE-${reportData.id}",
                        complainantName = "",
                        complainantContact = "",
                        complainantAddress = "",
                        incidentType = reportData.incidentType,
                        incidentDate = reportData.dateFiled,
                        incidentTime = "",
                        incidentLocation = reportData.incidentLocation,
                        narrative = reportData.narrative,
                        status = reportData.status,
                        dateFiled = reportData.dateFiled,
                        assignedOfficer = "",
                        assignedOfficerId = reportData.officerId,
                        assignedOfficerIds = reportData.officerId?.toString() ?: "",
                        isArchived = false,
                        audioUri = reportData.audioUri ?: "" // ✅ Audio synced from cloud!
                    )
                }
                Log.d(TAG, "✅ Fetched ${reports.size} reports from cloud")
                Result.success(reports)
            } else {
                Log.e(TAG, "❌ Failed to fetch reports: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching reports from cloud: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== Health Check ====================
    
    suspend fun healthCheck(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.healthCheck()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Log.e(TAG, "Health check error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== FCM Notifications ====================
    
    suspend fun sendBulkFCMNotification(
        title: String,
        message: String,
        recipientType: String,
        specificUserIds: List<Int>? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "📤 Sending bulk FCM notification")
            Log.d(TAG, "📋 Title: $title")
            Log.d(TAG, "📋 Recipient type: $recipientType")
            
            val requestBody = mapOf(
                "title" to title,
                "message" to message,
                "recipientType" to recipientType,
                "specificUserIds" to (specificUserIds ?: emptyList())
            )
            
            val response = apiService.sendBulkFCMNotification(requestBody)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data as? Map<*, *>
                val successCount = data?.get("successCount") as? Double ?: 0.0
                val failureCount = data?.get("failureCount") as? Double ?: 0.0
                
                Log.d(TAG, "✅ FCM notifications sent: Success=${successCount.toInt()}, Failed=${failureCount.toInt()}")
                Result.success(true)
            } else {
                Log.e(TAG, "❌ Failed to send FCM notifications: ${response.body()?.message}")
                Result.failure(Exception(response.body()?.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ FCM notification error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== App Version ====================
    
    suspend fun getAppVersion(): Result<com.example.blottermanagementsystem.data.api.VersionData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Checking app version...")
            val response = apiService.getAppVersion()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val versionData = response.body()?.data
                if (versionData != null) {
                    Log.d(TAG, "✅ Version info received: ${versionData.latestVersionName}")
                    Result.success(versionData)
                } else {
                    Result.failure(Exception("No version data"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get version"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Version check error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
