package com.example.blottermanagementsystem.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.blottermanagementsystem.data.entity.BlotterReport
import java.text.SimpleDateFormat
import java.util.*

/**
 * Email Helper
 * Sends email notifications for blotter reports
 */
object EmailHelper {
    
    /**
     * Send email using device's email client
     * @param context Android context
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body
     */
    fun sendEmail(
        context: Context,
        to: String,
        subject: String,
        body: String
    ) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Send Email"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Send report filed notification email
     */
    fun sendReportFiledEmail(
        context: Context,
        report: BlotterReport,
        recipientEmail: String
    ) {
        val subject = "Blotter Report Filed - ${report.caseNumber}"
        val body = buildReportFiledEmailBody(report)
        sendEmail(context, recipientEmail, subject, body)
    }
    
    /**
     * Send status update notification email
     */
    fun sendStatusUpdateEmail(
        context: Context,
        report: BlotterReport,
        recipientEmail: String,
        oldStatus: String,
        newStatus: String
    ) {
        val subject = "Status Update - ${report.caseNumber}"
        val body = buildStatusUpdateEmailBody(report, oldStatus, newStatus)
        sendEmail(context, recipientEmail, subject, body)
    }
    
    /**
     * Send case assigned notification email
     */
    fun sendCaseAssignedEmail(
        context: Context,
        report: BlotterReport,
        officerEmail: String,
        officerName: String
    ) {
        val subject = "Case Assigned - ${report.caseNumber}"
        val body = buildCaseAssignedEmailBody(report, officerName)
        sendEmail(context, officerEmail, subject, body)
    }
    
    /**
     * Send hearing scheduled notification email
     */
    fun sendHearingScheduledEmail(
        context: Context,
        report: BlotterReport,
        recipientEmail: String,
        hearingDate: String,
        hearingTime: String,
        location: String
    ) {
        val subject = "Hearing Scheduled - ${report.caseNumber}"
        val body = buildHearingScheduledEmailBody(report, hearingDate, hearingTime, location)
        sendEmail(context, recipientEmail, subject, body)
    }
    
    /**
     * Build email body for report filed notification
     */
    private fun buildReportFiledEmailBody(report: BlotterReport): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val filedDate = dateFormat.format(Date(report.dateFiled))
        val incidentDateFormatted = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(report.incidentDate))
        
        return """
            Dear ${report.complainantName},
            
            Your blotter report has been successfully filed.
            
            CASE DETAILS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Case Number: ${report.caseNumber}
            Filed Date: $filedDate
            Incident Type: ${report.incidentType}
            Status: ${report.status}
            
            INCIDENT INFORMATION:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Description: ${report.narrative}
            Location: ${report.incidentLocation}
            Date: $incidentDateFormatted
            Time: ${report.incidentTime}
            
            NEXT STEPS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Your case is now under review
            • An officer will be assigned shortly
            • You will receive updates via email and SMS
            • Please keep your case number for reference
            
            IMPORTANT REMINDERS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Do not reply to this email
            • For inquiries, visit the barangay office
            • Bring valid ID when following up
            • Keep all evidence and documents safe
            
            Thank you for your cooperation.
            
            Barangay Blotter Management System
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            This is an automated message. Please do not reply.
        """.trimIndent()
    }
    
    /**
     * Build email body for status update notification
     */
    private fun buildStatusUpdateEmailBody(
        report: BlotterReport,
        oldStatus: String,
        newStatus: String
    ): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val updateDate = dateFormat.format(Date())
        
        return """
            Dear ${report.complainantName},
            
            The status of your blotter report has been updated.
            
            CASE DETAILS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Case Number: ${report.caseNumber}
            Incident Type: ${report.incidentType}
            
            STATUS UPDATE:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Previous Status: $oldStatus
            New Status: $newStatus
            Updated: $updateDate
            
            ${getStatusMessage(newStatus)}
            
            NEXT STEPS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            ${getNextSteps(newStatus)}
            
            For more information, please visit the barangay office.
            
            Thank you for your cooperation.
            
            Barangay Blotter Management System
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            This is an automated message. Please do not reply.
        """.trimIndent()
    }
    
    /**
     * Build email body for case assigned notification
     */
    private fun buildCaseAssignedEmailBody(
        report: BlotterReport,
        officerName: String
    ): String {
        return """
            Dear Officer $officerName,
            
            A new case has been assigned to you.
            
            CASE DETAILS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Case Number: ${report.caseNumber}
            Incident Type: ${report.incidentType}
            Status: ${report.status}
            
            COMPLAINANT:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Name: ${report.complainantName}
            Contact: ${report.complainantContact}
            
            INCIDENT INFORMATION:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Description: ${report.narrative}
            Location: ${report.incidentLocation}
            Date: ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(report.incidentDate))}
            Time: ${report.incidentTime}
            
            REQUIRED ACTIONS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Review case details thoroughly
            • Contact complainant if needed
            • Conduct investigation
            • Update case status regularly
            • Submit findings and recommendations
            
            Please log in to the system to view full case details.
            
            Barangay Blotter Management System
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            This is an automated message. Please do not reply.
        """.trimIndent()
    }
    
    /**
     * Build email body for hearing scheduled notification
     */
    private fun buildHearingScheduledEmailBody(
        report: BlotterReport,
        hearingDate: String,
        hearingTime: String,
        location: String
    ): String {
        return """
            Dear ${report.complainantName},
            
            A hearing has been scheduled for your case.
            
            CASE DETAILS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Case Number: ${report.caseNumber}
            Incident Type: ${report.incidentType}
            
            HEARING SCHEDULE:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Date: $hearingDate
            Time: $hearingTime
            Location: $location
            
            WHAT TO BRING:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Valid government-issued ID
            • All relevant documents and evidence
            • Witnesses (if any)
            • Copy of your blotter report
            
            IMPORTANT REMINDERS:
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • Arrive 15 minutes before scheduled time
            • Dress appropriately
            • Be respectful and cooperative
            • Bring a copy of this notification
            • Failure to attend may result in case dismissal
            
            If you cannot attend, please inform the barangay office
            at least 24 hours before the scheduled hearing.
            
            Thank you for your cooperation.
            
            Barangay Blotter Management System
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            This is an automated message. Please do not reply.
        """.trimIndent()
    }
    
    /**
     * Get status-specific message
     */
    private fun getStatusMessage(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Your case is pending review by our officers."
            "under investigation" -> "Your case is currently under investigation."
            "resolved" -> "Your case has been resolved. Thank you for your patience."
            "closed" -> "Your case has been closed."
            "dismissed" -> "Your case has been dismissed."
            else -> "Your case status has been updated."
        }
    }
    
    /**
     * Get next steps based on status
     */
    private fun getNextSteps(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "• Wait for officer assignment\n• Prepare additional evidence if available"
            "under investigation" -> "• Cooperate with investigating officer\n• Provide additional information if requested"
            "resolved" -> "• Review resolution details\n• Provide feedback if needed"
            "closed" -> "• Case is now closed\n• Contact office if you have concerns"
            "dismissed" -> "• Case has been dismissed\n• You may file an appeal if needed"
            else -> "• Check your case status regularly\n• Contact office for inquiries"
        }
    }
    
    /**
     * Validate email address
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
