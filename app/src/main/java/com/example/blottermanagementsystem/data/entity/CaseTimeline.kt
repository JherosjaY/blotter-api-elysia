package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "case_timeline",
    foreignKeys = [
        ForeignKey(
            entity = BlotterReport::class,
            parentColumns = ["id"],
            childColumns = ["blotterReportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("blotterReportId")]
)
data class CaseTimeline(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val eventType: String, // "STATUS_CHANGE", "OFFICER_ASSIGNED", "HEARING_SCHEDULED", "EVIDENCE_ADDED", "RESPONDENT_ADDED", "RESOLUTION_ADDED"
    val eventTitle: String,
    val eventDescription: String,
    val performedBy: String, // User who performed the action
    val performedByRole: String, // "Admin", "Officer", "User"
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null // JSON for additional data
)
