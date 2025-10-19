package com.example.blottermanagementsystem.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_notifications",
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
data class SmsNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val respondentId: Int,
    val blotterReportId: Int,
    val messageType: String, // "Initial Notice", "Hearing Notice", "Reminder", "Final Notice"
    val messageContent: String,
    val recipientNumber: String,
    val sentDate: Long = System.currentTimeMillis(),
    val deliveryStatus: String = "Pending", // Pending, Delivered, Failed
    val respondentReply: String? = null,
    val replyDate: Long? = null
)

// Message Types Constants
object SmsMessageType {
    const val INITIAL_NOTICE = "Initial Notice"
    const val HEARING_NOTICE = "Hearing Notice"
    const val REMINDER = "Reminder"
    const val FINAL_NOTICE = "Final Notice"
}
