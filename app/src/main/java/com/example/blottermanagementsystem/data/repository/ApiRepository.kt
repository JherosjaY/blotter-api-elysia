package com.example.blottermanagementsystem.data.repository

import android.util.Log
import com.example.blottermanagementsystem.data.api.ApiConfig
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
            val response = apiService.login(LoginRequest(username, password))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()?.data
                if (loginData != null) {
                    // Convert LoginData to User model
                    val user = User(
                        id = loginData.id,
                        firstName = loginData.firstName,
                        lastName = loginData.lastName,
                        username = loginData.username,
                        password = "", // Don't store password from API
                        role = loginData.role,
                        profilePhotoUri = loginData.profilePhotoUri,
                        profileCompleted = true
                    )
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
            val request = RegisterRequest(username, password, firstName, lastName, role)
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val loginData = response.body()?.data
                if (loginData != null) {
                    val user = User(
                        id = loginData.id,
                        firstName = loginData.firstName,
                        lastName = loginData.lastName,
                        username = loginData.username,
                        password = "",
                        role = loginData.role,
                        profilePhotoUri = loginData.profilePhotoUri,
                        profileCompleted = false
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
                val reports = response.body()?.data ?: emptyList()
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
            val response = apiService.createReport(report)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val createdReport = response.body()?.data
                if (createdReport != null) {
                    Result.success(createdReport)
                } else {
                    Result.failure(Exception("Created report is null"))
                }
            } else {
                Result.failure(Exception("Failed to create report"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Create report error: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateReport(id: Int, report: BlotterReport): Result<BlotterReport> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateReport(id, report)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val updatedReport = response.body()?.data
                if (updatedReport != null) {
                    Result.success(updatedReport)
                } else {
                    Result.failure(Exception("Updated report is null"))
                }
            } else {
                Result.failure(Exception("Failed to update report"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Update report error: ${e.message}", e)
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
}
