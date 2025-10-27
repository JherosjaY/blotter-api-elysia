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
    
    @POST("api/users/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<ApiResponse<String>>
    
    @PUT("api/auth/profile/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: Int,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserData>>
    
    // ==================== Officers ====================
    
    @GET("api/officers")
    suspend fun getAllOfficers(): Response<ApiResponse<List<OfficerData>>>
    
    @POST("api/officers")
    suspend fun createOfficer(@Body officer: com.example.blottermanagementsystem.data.entity.Officer): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Officer>>
    
    @PUT("api/officers/{id}")
    suspend fun updateOfficer(
        @Path("id") id: Int,
        @Body officer: com.example.blottermanagementsystem.data.entity.Officer
    ): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Officer>>
    
    @DELETE("api/officers/{id}")
    suspend fun deleteOfficer(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Notifications ====================
    
    @GET("api/notifications/{userId}")
    suspend fun getUserNotifications(@Path("userId") userId: Int): Response<ApiResponse<List<NotificationData>>>
    
    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: Int): Response<ApiResponse<String>>
    
    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Blotter Reports ====================
    
    @GET("api/reports")
    suspend fun getAllReports(): Response<ApiResponse<List<ReportData>>>
    
    @GET("api/reports/{id}")
    suspend fun getReportById(@Path("id") id: Int): Response<ApiResponse<BlotterReport>>
    
    @POST("api/reports")
    suspend fun createReport(@Body report: BlotterReport): Response<ApiResponse<BlotterReport>>
    
    @POST("api/reports")
    suspend fun createReportRaw(@Body report: Map<String, Any?>): Response<ApiResponse<BlotterReport>>
    
    @PUT("api/reports/{id}")
    suspend fun updateReport(
        @Path("id") id: Int,
        @Body report: BlotterReport
    ): Response<ApiResponse<BlotterReport>>
    
    @DELETE("api/reports/{id}")
    suspend fun deleteReport(@Path("id") id: Int): Response<ApiResponse<String>>
    
    @GET("api/reports/status/{status}")
    suspend fun getReportsByStatus(@Path("status") status: String): Response<ApiResponse<List<BlotterReport>>>
    
    // ==================== Evidence ====================
    
    @GET("api/evidence")
    suspend fun getAllEvidence(): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.Evidence>>>
    
    @GET("api/evidence/report/{reportId}")
    suspend fun getEvidenceByReportId(@Path("reportId") reportId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.Evidence>>>
    
    @GET("api/evidence/{id}")
    suspend fun getEvidenceById(@Path("id") id: Int): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Evidence>>
    
    @POST("api/evidence")
    suspend fun createEvidence(@Body evidence: com.example.blottermanagementsystem.data.entity.Evidence): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Evidence>>
    
    @PUT("api/evidence/{id}")
    suspend fun updateEvidence(
        @Path("id") id: Int,
        @Body evidence: com.example.blottermanagementsystem.data.entity.Evidence
    ): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Evidence>>
    
    @DELETE("api/evidence/{id}")
    suspend fun deleteEvidence(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Witnesses ====================
    
    @GET("api/witnesses/report/{reportId}")
    suspend fun getWitnessesByReportId(@Path("reportId") reportId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.Witness>>>
    
    @POST("api/witnesses")
    suspend fun createWitness(@Body witness: com.example.blottermanagementsystem.data.entity.Witness): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Witness>>
    
    @DELETE("api/witnesses/{id}")
    suspend fun deleteWitness(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Suspects ====================
    
    @GET("api/suspects/report/{reportId}")
    suspend fun getSuspectsByReportId(@Path("reportId") reportId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.Suspect>>>
    
    @POST("api/suspects")
    suspend fun createSuspect(@Body suspect: com.example.blottermanagementsystem.data.entity.Suspect): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Suspect>>
    
    @DELETE("api/suspects/{id}")
    suspend fun deleteSuspect(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Resolutions ====================
    
    @GET("api/resolutions/report/{reportId}")
    suspend fun getResolutionsByReportId(@Path("reportId") reportId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.Resolution>>>
    
    @POST("api/resolutions")
    suspend fun createResolution(@Body resolution: com.example.blottermanagementsystem.data.entity.Resolution): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Resolution>>
    
    // ==================== Respondents ====================
    
    @GET("api/respondents/report/{reportId}")
    suspend fun getRespondentsByReportId(@Path("reportId") reportId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.Respondent>>>
    
    @POST("api/respondents")
    suspend fun createRespondent(@Body respondent: com.example.blottermanagementsystem.data.entity.Respondent): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.Respondent>>
    
    @DELETE("api/respondents/{id}")
    suspend fun deleteRespondent(@Path("id") id: Int): Response<ApiResponse<String>>
    
    // ==================== Activity Logs ====================
    
    @GET("api/activity-logs")
    suspend fun getAllActivityLogs(): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.ActivityLog>>>
    
    @GET("api/activity-logs/case/{caseId}")
    suspend fun getActivityLogsByCaseId(@Path("caseId") caseId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.ActivityLog>>>
    
    @POST("api/activity-logs")
    suspend fun createActivityLog(@Body activityLog: com.example.blottermanagementsystem.data.entity.ActivityLog): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.ActivityLog>>
    
    // ==================== Dashboard ====================
    
    @GET("api/dashboard/analytics")
    suspend fun getDashboardAnalytics(): Response<ApiResponse<DashboardAnalyticsData>>
    
    // ==================== Person History ====================
    
    @GET("api/persons/{personId}/history")
    suspend fun getPersonHistory(@Path("personId") personId: Int): Response<ApiResponse<List<com.example.blottermanagementsystem.data.entity.PersonHistory>>>
    
    @POST("api/persons/history")
    suspend fun createPersonHistory(@Body history: com.example.blottermanagementsystem.data.entity.PersonHistory): Response<ApiResponse<com.example.blottermanagementsystem.data.entity.PersonHistory>>
    
    // ==================== Health Check ====================
    
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<Map<String, String>>>
    
    // ==================== FCM Notifications ====================
    
    @POST("api/notifications/fcm/send-bulk")
    suspend fun sendBulkFCMNotification(
        @Body request: Map<String, Any>
    ): Response<ApiResponse<Map<String, Any>>>
    
    // ==================== App Version ====================
    
    @GET("api/version")
    suspend fun getAppVersion(): Response<ApiResponse<VersionData>>
}
