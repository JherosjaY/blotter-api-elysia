package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "respondent_statements",
    foreignKeys = [
        ForeignKey(
            entity = Respondent::class,
            parentColumns = ["id"],
            childColumns = ["respondentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BlotterReport::class,
            parentColumns = ["id"],
            childColumns = ["blotterReportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("respondentId"), Index("blotterReportId")]
)
data class RespondentStatement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val respondentId: Int,
    val blotterReportId: Int,
    val statement: String,
    val evidenceUris: String? = null, // JSON array of URIs
    val submittedDate: Long = System.currentTimeMillis(),
    val submittedVia: String = "In Person", // "In Person", "App", "Phone", "Written"
    val isVerified: Boolean = false,
    val verifiedBy: String? = null,
    val verifiedDate: Long? = null,
    val officerNotes: String? = null
)
