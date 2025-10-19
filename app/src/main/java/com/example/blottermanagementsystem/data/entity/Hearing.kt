package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hearings",
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
data class Hearing(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val hearingDate: String,
    val hearingTime: String,
    val location: String,
    val purpose: String,
    val status: String = "Scheduled",
    val createdAt: Long = System.currentTimeMillis()
)
