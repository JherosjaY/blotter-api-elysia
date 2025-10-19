package com.example.blottermanagementsystem.data.api.models

import com.google.gson.annotations.SerializedName

// ========== BASE RESPONSE ==========
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("error") val error: String? = null
)

// ========== AUTH MODELS ==========
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("profile_picture") val profilePicture: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("role") val role: String = "User"
)

// ========== REPORT MODELS ==========
data class ReportApiModel(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String?,
    @SerializedName("blotter_number") val blotterNumber: String?,
    @SerializedName("incident_type") val incidentType: String,
    @SerializedName("incident_date") val incidentDate: String,
    @SerializedName("incident_time") val incidentTime: String,
    @SerializedName("location") val location: String,
    @SerializedName("description") val description: String,
    @SerializedName("complainant_name") val complainantName: String,
    @SerializedName("complainant_contact") val complainantContact: String? = null,
    @SerializedName("complainant_address") val complainantAddress: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("assigned_officer_id") val assignedOfficerId: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateReportRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("incidentType") val incidentType: String,
    @SerializedName("incidentDate") val incidentDate: String,
    @SerializedName("incidentTime") val incidentTime: String,
    @SerializedName("location") val location: String,
    @SerializedName("description") val description: String,
    @SerializedName("complainantName") val complainantName: String,
    @SerializedName("complainantContact") val complainantContact: String? = null,
    @SerializedName("complainantAddress") val complainantAddress: String? = null,
    @SerializedName("priority") val priority: String = "Normal"
)

// ========== USER MODELS ==========
data class UserApiModel(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String
)

// ========== OFFICER MODELS ==========
data class OfficerApiModel(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("badge_number") val badgeNumber: String? = null,
    @SerializedName("rank") val rank: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("assigned_cases_count") val assignedCasesCount: Int = 0,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

// ========== RESPONDENT MODELS ==========
data class RespondentApiModel(
    @SerializedName("id") val id: String,
    @SerializedName("report_id") val reportId: String,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("contact") val contact: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("cooperation_status") val cooperationStatus: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("created_at") val createdAt: String
)

data class CreateRespondentRequest(
    @SerializedName("reportId") val reportId: String,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("contact") val contact: String? = null,
    @SerializedName("address") val address: String? = null
)

// ========== ANALYTICS MODELS ==========
data class DashboardAnalytics(
    @SerializedName("totalReports") val totalReports: Int,
    @SerializedName("pendingReports") val pendingReports: Int,
    @SerializedName("resolvedReports") val resolvedReports: Int,
    @SerializedName("totalOfficers") val totalOfficers: Int,
    @SerializedName("totalUsers") val totalUsers: Int,
    @SerializedName("activeReports") val activeReports: Int
)
