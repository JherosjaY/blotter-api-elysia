package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val firstName: String,
    val lastName: String,
    val username: String,
    val password: String, // Hashed password
    val role: String, // Admin, Officer, Clerk
    val profilePhotoUri: String? = null,
    val gender: String? = null,
    val accountCreated: Long = System.currentTimeMillis(),
    val profileCompleted: Boolean = false,
    val badgeNumber: String? = null,
    val rank: String? = null,
    val dutyStatus: String? = null,
    val mustChangePassword: Boolean = false,
    val isActive: Boolean = true,
    val fcmToken: String? = null,
    val deviceId: String? = null
)
