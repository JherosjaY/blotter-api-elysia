package com.example.blottermanagementsystem.data.api

import com.example.blottermanagementsystem.data.entity.BlotterReport
import com.example.blottermanagementsystem.data.entity.User
import retrofit2.Response
import retrofit2.http.*

/**
 * Blotter API Service - Elysia.js Backend
 */
interface BlotterApiService {
    
    // ==================== Authentication ====================
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginData>>
    
    // ==================== Users ====================
    
    @GET("api/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<User>>>
    
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<ApiResponse<User>>
    
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: User
    ): Response<ApiResponse<User>>
    
    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Blotter Reports ====================
    
    @GET("api/reports")
    suspend fun getAllReports(): Response<ApiResponse<List<BlotterReport>>>
    
    @GET("api/reports/{id}")
    suspend fun getReportById(@Path("id") id: Int): Response<ApiResponse<BlotterReport>>
    
    @POST("api/reports")
    suspend fun createReport(@Body report: BlotterReport): Response<ApiResponse<BlotterReport>>
    
    @PUT("api/reports/{id}")
    suspend fun updateReport(
        @Path("id") id: Int,
        @Body report: BlotterReport
    ): Response<ApiResponse<BlotterReport>>
    
    @DELETE("api/reports/{id}")
    suspend fun deleteReport(@Path("id") id: Int): Response<ApiResponse<String>>
    
    @GET("api/reports/status/{status}")
    suspend fun getReportsByStatus(@Path("status") status: String): Response<ApiResponse<List<BlotterReport>>>
    
    // ==================== Health Check ====================
    
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<Map<String, String>>>
}
