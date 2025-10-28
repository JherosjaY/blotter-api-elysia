package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SyncQueue - Tracks pending data that needs to be synced to cloud
 * Used for offline-first architecture
 */
@Entity(tableName = "sync_queue")
data class SyncQueue(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val entityType: String, // "Report", "Respondent", "Evidence", etc.
    val entityId: Int, // Local ID of the entity
    val action: String, // "CREATE", "UPDATE", "DELETE"
    val data: String, // JSON string of the entity data
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastError: String? = null,
    val synced: Boolean = false
)
