package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "summons",
    foreignKeys = [
        ForeignKey(
            entity = BlotterReport::class,
            parentColumns = ["id"],
            childColumns = ["blotterReportId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Respondent::class,
            parentColumns = ["id"],
            childColumns = ["respondentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("blotterReportId"), Index("respondentId")]
)
data class Summons(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val respondentId: Int,
    val summonsNumber: String, // Format: SUM-YYYY-MM-XXXX
    val summonsType: String, // "First Notice", "Second Notice", "Final Notice", "Hearing Summons"
    val issueDate: Long = System.currentTimeMillis(),
    val hearingDate: Long? = null,
    val hearingTime: String? = null,
    val hearingVenue: String = "Barangay Hall",
    val purpose: String, // "Mediation", "Hearing", "Settlement Conference"
    val issuedBy: String, // Barangay Captain/Lupon Chairman name
    val issuedByPosition: String = "Punong Barangay",
    val receivedDate: Long? = null,
    val receivedBy: String? = null, // Who received the summons
    val receivedByRelation: String? = null, // Relation to respondent
    val deliveryMethod: String = "Personal", // "Personal", "Registered Mail", "Posted"
    val deliveryStatus: String = "Pending", // "Pending", "Served", "Refused", "Not Found"
    val deliveryNotes: String? = null,
    val returnDate: Long? = null, // When summons was returned
    val isComplied: Boolean = false, // Did respondent appear?
    val complianceDate: Long? = null,
    val complianceNotes: String? = null,
    val documentPath: String? = null // Path to generated PDF
)
