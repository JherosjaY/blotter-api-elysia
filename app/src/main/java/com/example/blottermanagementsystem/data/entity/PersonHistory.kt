package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_history",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BlotterReport::class,
            parentColumns = ["id"],
            childColumns = ["blotterReportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId"), Index("blotterReportId")]
)
data class PersonHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val personId: Int, // Who is this about?
    val blotterReportId: Int? = null,
    val activityType: String, // FILED_REPORT, ADDED_AS_RESPONDENT, ELEVATED_TO_SUSPECT, etc.
    val description: String,
    val performedByPersonId: Int? = null, // Who did this action?
    val oldValue: String? = null,
    val newValue: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null // JSON for extra data
)

// Activity Types Constants
object PersonActivityType {
    const val FILED_REPORT = "FILED_REPORT"
    const val ADDED_AS_RESPONDENT = "ADDED_AS_RESPONDENT"
    const val ADDED_AS_WITNESS = "ADDED_AS_WITNESS"
    const val ASSIGNED_AS_OFFICER = "ASSIGNED_AS_OFFICER"
    const val ELEVATED_TO_SUSPECT = "ELEVATED_TO_SUSPECT"
    const val STATUS_CHANGED = "STATUS_CHANGED"
    const val REPORT_UPDATED = "REPORT_UPDATED"
    const val EVIDENCE_ADDED = "EVIDENCE_ADDED"
    const val CASE_RESOLVED = "CASE_RESOLVED"
    const val APPEARED_IN_PERSON = "APPEARED_IN_PERSON"
    const val STATEMENT_GIVEN = "STATEMENT_GIVEN"
    const val HEARING_SCHEDULED = "HEARING_SCHEDULED"
    const val ATTENDED_HEARING = "ATTENDED_HEARING"
    const val CLEARED = "CLEARED"
    const val CONVICTED = "CONVICTED"
}
