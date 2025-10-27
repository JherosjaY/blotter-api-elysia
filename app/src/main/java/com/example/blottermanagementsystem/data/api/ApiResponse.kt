package com.example.blottermanagementsystem.data.api

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * Login Request
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * Login Response Data (wrapper with user and token)
 */
data class LoginData(
    @SerializedName("user")
    val user: UserData,
    
    @SerializedName("token")
    val token: String?
)

/**
 * User Data from API
 */
data class UserData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("firstName")
    val firstName: String?,
    
    @SerializedName("lastName")
    val lastName: String?,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("profilePhotoUri")
    val profilePhotoUri: String?,
    
    @SerializedName("profileCompleted")
    val profileCompleted: Boolean? = false,
    
    @SerializedName("isActive")
    val isActive: Boolean? = true,
    
    @SerializedName("mustChangePassword")
    val mustChangePassword: Boolean? = false,
    
    @SerializedName("fcmToken")
    val fcmToken: String? = null,
    
    @SerializedName("deviceId")
    val deviceId: String? = null
)

/**
 * Register Request
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("role")
    val role: String
)

/**
 * FCM Token Request
 */
data class FcmTokenRequest(
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("fcmToken")
    val fcmToken: String,
    
    @SerializedName("deviceId")
    val deviceId: String? = null
)

/**
 * Update Profile Request
 */
data class UpdateProfileRequest(
    @SerializedName("profilePhotoUri")
    val profilePhotoUri: String,
    
    @SerializedName("profileCompleted")
    val profileCompleted: Boolean = true
)

/**
 * Officer Data from API
 */
data class OfficerData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("badgeNumber")
    val badgeNumber: String?,
    
    @SerializedName("rank")
    val rank: String?,
    
    @SerializedName("station")
    val station: String? = null,
    
    @SerializedName("specialization")
    val specialization: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean? = true
)

/**
 * Report Data from API
 */
data class ReportData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("officerId")
    val officerId: Int? = null,
    
    @SerializedName("incidentType")
    val incidentType: String,
    
    @SerializedName("narrative")
    val narrative: String,
    
    @SerializedName("incidentLocation")
    val incidentLocation: String,
    
    @SerializedName("dateFiled")
    val dateFiled: Long,
    
    @SerializedName("incidentDate")
    val incidentDate: String? = null,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("caseNumber")
    val caseNumber: String? = null,
    
    @SerializedName("priority")
    val priority: String? = "Normal",
    
    @SerializedName("audioUri")
    val audioUri: String? = null
)

/**
 * Notification Data from API
 */
data class NotificationData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("caseId")
    val caseId: Int? = null,
    
    @SerializedName("isRead")
    val isRead: Boolean = false,
    
    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * Dashboard Analytics Data
 */
data class DashboardAnalyticsData(
    @SerializedName("totalReports")
    val totalReports: Int = 0,
    
    @SerializedName("pendingReports")
    val pendingReports: Int = 0,
    
    @SerializedName("ongoingReports")
    val ongoingReports: Int = 0,
    
    @SerializedName("resolvedReports")
    val resolvedReports: Int = 0,
    
    @SerializedName("archivedReports")
    val archivedReports: Int = 0,
    
    @SerializedName("totalOfficers")
    val totalOfficers: Int = 0,
    
    @SerializedName("totalUsers")
    val totalUsers: Int = 0
)

/**
 * Version Data from API
 */
data class VersionData(
    @SerializedName("latestVersion")
    val latestVersion: Int,
    
    @SerializedName("latestVersionName")
    val latestVersionName: String,
    
    @SerializedName("minimumVersion")
    val minimumVersion: Int,
    
    @SerializedName("forceUpdate")
    val forceUpdate: Boolean = false,
    
    @SerializedName("updateMessage")
    val updateMessage: String,
    
    @SerializedName("updateUrl")
    val updateUrl: String
)
