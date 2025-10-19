package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val caseId: Int? = null,
    val caseTitle: String? = null,
    val activityType: String, // e.g., "CREATE", "UPDATE", "DELETE", "ARCHIVE"
    val description: String,
    val oldValue: String? = null,
    val newValue: String? = null,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)
