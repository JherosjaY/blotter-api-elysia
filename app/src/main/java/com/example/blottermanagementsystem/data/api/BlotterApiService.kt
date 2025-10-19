package com.example.blottermanagementsystem.data.api

import com.example.blottermanagementsystem.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface BlotterApiService {
    
    // ========== AUTHENTICATION ==========
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>
    
    @GET("api/auth/me/{userId}")
    suspend fun getCurrentUser(@Path("userId") userId: String): Response<ApiResponse<LoginResponse>>
    
    // ========== REPORTS ==========
    @GET("api/reports")
    suspend fun getAllReports(): Response<ApiResponse<List<ReportApiModel>>>
    
    @GET("api/reports/{id}")
    suspend fun getReportById(@Path("id") id: String): Response<ApiResponse<ReportApiModel>>
    
    @GET("api/reports/user/{userId}")
    suspend fun getReportsByUser(@Path("userId") userId: String): Response<ApiResponse<List<ReportApiModel>>>
    
    @POST("api/reports")
    suspend fun createReport(@Body request: CreateReportRequest): Response<ApiResponse<ReportApiModel>>
    
    @PUT("api/reports/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Body report: Map<String, Any>
    ): Response<ApiResponse<ReportApiModel>>
    
    @PATCH("api/reports/{id}/assign-officer")
    suspend fun assignOfficer(
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<ApiResponse<ReportApiModel>>
    
    @PATCH("api/reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<ApiResponse<ReportApiModel>>
    
    @DELETE("api/reports/{id}")
    suspend fun deleteReport(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== USERS ==========
    @GET("api/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserApiModel>>>
    
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ApiResponse<UserApiModel>>
    
    @POST("api/users")
    suspend fun createUser(@Body user: Map<String, Any>): Response<ApiResponse<UserApiModel>>
    
    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: Map<String, Any>
    ): Response<ApiResponse<UserApiModel>>
    
    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== OFFICERS ==========
    @GET("api/officers")
    suspend fun getAllOfficers(): Response<ApiResponse<List<OfficerApiModel>>>
    
    @GET("api/officers/{id}")
    suspend fun getOfficerById(@Path("id") id: String): Response<ApiResponse<OfficerApiModel>>
    
    @POST("api/officers")
    suspend fun createOfficer(@Body officer: Map<String, Any>): Response<ApiResponse<OfficerApiModel>>
    
    @PUT("api/officers/{id}")
    suspend fun updateOfficer(
        @Path("id") id: String,
        @Body officer: Map<String, Any>
    ): Response<ApiResponse<OfficerApiModel>>
    
    @DELETE("api/officers/{id}")
    suspend fun deleteOfficer(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== RESPONDENTS ==========
    @GET("api/respondents/report/{reportId}")
    suspend fun getRespondentsByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<RespondentApiModel>>>
    
    @POST("api/respondents")
    suspend fun createRespondent(@Body request: CreateRespondentRequest): Response<ApiResponse<RespondentApiModel>>
    
    @DELETE("api/respondents/{id}")
    suspend fun deleteRespondent(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== SUSPECTS ==========
    @GET("api/suspects/report/{reportId}")
    suspend fun getSuspectsByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<Any>>>
    
    @POST("api/suspects")
    suspend fun createSuspect(@Body suspect: Map<String, Any>): Response<ApiResponse<Any>>
    
    @DELETE("api/suspects/{id}")
    suspend fun deleteSuspect(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== WITNESSES ==========
    @GET("api/witnesses/report/{reportId}")
    suspend fun getWitnessesByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<Any>>>
    
    @POST("api/witnesses")
    suspend fun createWitness(@Body witness: Map<String, Any>): Response<ApiResponse<Any>>
    
    @DELETE("api/witnesses/{id}")
    suspend fun deleteWitness(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== EVIDENCE ==========
    @GET("api/evidence/report/{reportId}")
    suspend fun getEvidenceByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<Any>>>
    
    @POST("api/evidence")
    suspend fun createEvidence(@Body evidence: Map<String, Any>): Response<ApiResponse<Any>>
    
    @DELETE("api/evidence/{id}")
    suspend fun deleteEvidence(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== HEARINGS ==========
    @GET("api/hearings")
    suspend fun getAllHearings(): Response<ApiResponse<List<Any>>>
    
    @GET("api/hearings/report/{reportId}")
    suspend fun getHearingsByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<Any>>>
    
    @POST("api/hearings")
    suspend fun createHearing(@Body hearing: Map<String, Any>): Response<ApiResponse<Any>>
    
    @DELETE("api/hearings/{id}")
    suspend fun deleteHearing(@Path("id") id: String): Response<ApiResponse<Unit>>
    
    // ========== RESOLUTIONS ==========
    @GET("api/resolutions/report/{reportId}")
    suspend fun getResolutionsByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<Any>>>
    
    @POST("api/resolutions")
    suspend fun createResolution(@Body resolution: Map<String, Any>): Response<ApiResponse<Any>>
    
    // ========== SMS ==========
    @GET("api/sms/report/{reportId}")
    suspend fun getSmsByReport(@Path("reportId") reportId: String): Response<ApiResponse<List<Any>>>
    
    @POST("api/sms/send")
    suspend fun sendSms(@Body sms: Map<String, Any>): Response<ApiResponse<Any>>
    
    // ========== ANALYTICS ==========
    @GET("api/analytics/dashboard")
    suspend fun getDashboardAnalytics(): Response<ApiResponse<DashboardAnalytics>>
    
    @GET("api/analytics/officer/{officerId}")
    suspend fun getOfficerAnalytics(@Path("officerId") officerId: String): Response<ApiResponse<Any>>
}
