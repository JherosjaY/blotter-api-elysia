package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resolutions",
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
data class Resolution(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val resolutionType: String,
    val resolutionDetails: String,
    val resolvedBy: Int, // User ID
    val resolvedDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
