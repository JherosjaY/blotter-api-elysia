package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blotter_reports",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BlotterReport(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val caseNumber: String,
    val complainantName: String,
    val complainantContact: String,
    val complainantAddress: String,
    val incidentType: String,
    val incidentDate: Long,
    val incidentTime: String,
    val incidentLocation: String,
    val narrative: String,
    val respondentName: String = "N/A",
    val respondentAddress: String = "N/A",
    val status: String = "Pending",
    val dateFiled: Long = System.currentTimeMillis(),
    val assignedOfficer: String = "", // Deprecated: Use assignedOfficerIds
    val assignedOfficerId: Int? = null, // Deprecated: Use assignedOfficerIds
    val assignedOfficerIds: String = "", // Comma-separated officer IDs (max 2): "1,5"
    val userId: Int, // Foreign key to users table
    val isArchived: Boolean = false,
    val archivedDate: Long? = null,
    val archivedBy: String? = null,
    val archivedReason: String? = null,
    // Evidence fields (stored as JSON strings)
    val imageUris: String = "", // JSON array of image URI strings
    val videoUris: String = "", // JSON array of video URI strings
    val videoDurations: String = "" // JSON object mapping video URIs to durations
)
