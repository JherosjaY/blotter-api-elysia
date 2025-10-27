package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "officers")
data class Officer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int? = null, // Link to User table (for login account)
    val name: String,
    val rank: String,
    val badgeNumber: String,
    val contactNumber: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val assignedCases: Int = 0, // Number of assigned cases
    val isAvailable: Boolean = true,
    val isActive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)
