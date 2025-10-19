package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val type: String, // e.g., "STATUS_UPDATE", "HEARING_REMINDER", "NEW_CASE"
    val caseId: Int? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
