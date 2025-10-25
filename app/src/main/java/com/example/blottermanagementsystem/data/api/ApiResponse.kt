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
 * Login Response Data
 */
data class LoginData(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("firstName")
    val firstName: String,
    
    @SerializedName("lastName")
    val lastName: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("profilePhotoUri")
    val profilePhotoUri: String?
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
