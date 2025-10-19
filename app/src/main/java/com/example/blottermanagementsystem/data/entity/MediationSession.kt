package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mediation_sessions",
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
data class MediationSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val sessionNumber: Int, // 1st, 2nd, 3rd attempt
    val sessionDate: Long,
    val sessionTime: String,
    val venue: String = "Barangay Hall",
    val mediatorName: String, // Lupon Chairman or assigned mediator
    val mediatorPosition: String = "Lupon Chairman",
    
    // Attendance
    val complainantPresent: Boolean = false,
    val complainantRepresentative: String? = null,
    val respondentPresent: Boolean = false,
    val respondentRepresentative: String? = null,
    val luponMembersPresent: String? = null, // JSON array of member names
    val witnessesPresent: String? = null, // JSON array of witness names
    
    // Session details
    val sessionType: String, // "Conciliation", "Mediation", "Arbitration"
    val discussionSummary: String,
    val agreementsReached: String? = null,
    val nextSteps: String? = null,
    
    // Outcome
    val outcome: String, // "Settled", "Partially Settled", "Failed", "Adjourned", "Ongoing"
    val settlementTerms: String? = null,
    val reasonForFailure: String? = null,
    
    // Next session
    val nextSessionScheduled: Boolean = false,
    val nextSessionDate: Long? = null,
    val nextSessionTime: String? = null,
    
    // Documentation
    val minutesOfMeeting: String? = null,
    val attachments: String? = null, // JSON array of file paths
    val recordedBy: String,
    val recordedDate: Long = System.currentTimeMillis(),
    
    // Signatures
    val complainantSignature: String? = null,
    val respondentSignature: String? = null,
    val mediatorSignature: String? = null
)
