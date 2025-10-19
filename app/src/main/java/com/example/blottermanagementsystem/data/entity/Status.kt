package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status")
data class Status(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String, // e.g., "Pending", "Under Investigation", "Resolved", "Archived"
    val description: String? = null,
    val color: String? = null // Hex color for UI display
)
