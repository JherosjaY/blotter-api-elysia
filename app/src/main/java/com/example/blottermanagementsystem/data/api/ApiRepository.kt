package com.example.blottermanagementsystem.data.api

import android.util.Log
import com.example.blottermanagementsystem.data.api.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository {
    private val apiService = ApiConfig.apiService
    
    // ========== AUTHENTICATION ==========
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Login failed"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Login error", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun register(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String = "User"
    ): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(
                    RegisterRequest(username, password, firstName, lastName, role)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Registration failed"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Register error", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== REPORTS ==========
    suspend fun getAllReports(): Result<List<ReportApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllReports()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to fetch reports"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Get reports error", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun getReportsByUser(userId: String): Result<List<ReportApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getReportsByUser(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to fetch user reports"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Get user reports error", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun createReport(request: CreateReportRequest): Result<ReportApiModel> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createReport(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to create report"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Create report error", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== USERS ==========
    suspend fun getAllUsers(): Result<List<UserApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllUsers()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to fetch users"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Get users error", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== OFFICERS ==========
    suspend fun getAllOfficers(): Result<List<OfficerApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllOfficers()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to fetch officers"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Get officers error", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== RESPONDENTS ==========
    suspend fun getRespondentsByReport(reportId: String): Result<List<RespondentApiModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRespondentsByReport(reportId)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.success(emptyList())
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to fetch respondents"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Get respondents error", e)
                Result.failure(e)
            }
        }
    }
    
    suspend fun createRespondent(request: CreateRespondentRequest): Result<RespondentApiModel> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createRespondent(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to create respondent"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Create respondent error", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== ANALYTICS ==========
    suspend fun getDashboardAnalytics(): Result<DashboardAnalytics> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDashboardAnalytics()
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("No data received"))
                } else {
                    Result.failure(Exception(response.body()?.message ?: "Failed to fetch analytics"))
                }
            } catch (e: Exception) {
                Log.e("ApiRepository", "Get analytics error", e)
                Result.failure(e)
            }
        }
    }
}
