package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kp_forms",
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
data class KPForm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val formType: String, // "KP-7", "KP-10", "KP-16", "KP-18"
    val formNumber: String, // Unique form number
    val formTitle: String, // "Notice of Hearing", "Summons", "Amicable Settlement", "Certificate to File Action"
    val issueDate: Long = System.currentTimeMillis(),
    val issuedBy: String, // Lupon Chairman/Punong Barangay
    val issuedByPosition: String = "Punong Barangay",
    
    // KP-7 & KP-10 specific fields
    val hearingDate: Long? = null,
    val hearingTime: String? = null,
    val hearingVenue: String? = null,
    
    // KP-16 specific fields (Amicable Settlement)
    val settlementTerms: String? = null, // JSON or text of settlement terms
    val complainantSignature: String? = null, // Path to signature image
    val respondentSignature: String? = null,
    val witnessSignatures: String? = null, // JSON array of witness signature paths
    val luponSignatures: String? = null, // JSON array of Lupon member signatures
    val settlementDate: Long? = null,
    
    // KP-18 specific fields (Certificate to File Action)
    val certificationReason: String? = null, // Why case failed to settle
    val attemptsMade: Int = 0, // Number of mediation attempts
    val lastAttemptDate: Long? = null,
    
    // Common fields
    val status: String = "Draft", // "Draft", "Issued", "Signed", "Completed", "Void"
    val notes: String? = null,
    val documentPath: String? = null, // Path to generated PDF
    val createdBy: String,
    val createdDate: Long = System.currentTimeMillis(),
    val lastModifiedDate: Long = System.currentTimeMillis()
)
