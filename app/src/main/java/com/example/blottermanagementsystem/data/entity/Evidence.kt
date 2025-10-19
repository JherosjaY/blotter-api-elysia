package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidence",
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
data class Evidence(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val evidenceType: String,
    val description: String,
    val filePath: String? = null,
    val collectedDate: Long = System.currentTimeMillis(),
    val collectedBy: String? = null,
    val locationFound: String? = null,
    val chainOfCustodyNotes: String? = null,
    val photoUris: String = "", // Comma-separated photo URIs
    val videoUris: String = "", // Comma-separated video URIs
    val capturedBy: String? = null, // Officer who took photos/videos
    val captureTimestamp: Long = System.currentTimeMillis()
)
