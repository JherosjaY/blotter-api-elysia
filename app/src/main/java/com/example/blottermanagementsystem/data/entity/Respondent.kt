package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "respondents",
    foreignKeys = [
        ForeignKey(
            entity = BlotterReport::class,
            parentColumns = ["id"],
            childColumns = ["blotterReportId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("blotterReportId"), Index("personId")]
)
data class Respondent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val blotterReportId: Int,
    val personId: Int,
    val accusation: String,
    val relationshipToComplainant: String? = null,
    val hasEvidence: Boolean = false,
    val status: String = "Accused", // Accused, Under Investigation, Cleared, Elevated to Suspect
    
    // Notification tracking
    val contactNumber: String,
    val notificationSent: Boolean = false,
    val notificationSentDate: Long? = null,
    val smsDelivered: Boolean = false,
    
    // Cooperation tracking
    val cooperationStatus: String = "Not Contacted", // Not Contacted, Notified, Acknowledged, Appeared, No Response, Refused
    val acknowledgedDate: Long? = null,
    val appearedInPerson: Boolean = false,
    val appearanceDate: Long? = null,
    val statementGiven: Boolean = false,
    val statementDate: Long? = null,
    val statement: String? = null,
    
    // Hearing tracking
    val hearingScheduled: Boolean = false,
    val hearingDate: Long? = null,
    val attendedHearing: Boolean = false,
    
    val dateAccused: Long = System.currentTimeMillis(),
    val notes: String? = null
)
