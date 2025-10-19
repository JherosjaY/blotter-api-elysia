package com.example.blottermanagementsystem.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.blottermanagementsystem.data.entity.SmsNotification

/**
 * SMS Helper - Sends actual SMS messages
 * Uses your phone's SMS (₱1 per message)
 */
object SmsHelper {
    
    /**
     * Check if SMS permission is granted
     */
    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Send SMS message
     * @return true if sent successfully, false if failed
     */
    fun sendSms(
        phoneNumber: String,
        message: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ): Boolean {
        try {
            // Validate phone number
            if (!isValidPhilippineNumber(phoneNumber)) {
                onFailure("Invalid Philippine phone number")
                return false
            }
            
            // Get SMS Manager
            val smsManager = SmsManager.getDefault()
            
            // If message is too long, split it
            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            } else {
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }
            
            onSuccess()
            return true
            
        } catch (e: Exception) {
            onFailure(e.message ?: "Failed to send SMS")
            return false
        }
    }
    
    /**
     * Validate Philippine phone number
     * Accepts: +639171234567, 09171234567, 9171234567
     */
    fun isValidPhilippineNumber(number: String): Boolean {
        val cleaned = number.replace(Regex("[^0-9+]"), "")
        
        return when {
            // +639171234567 (13 digits with +63)
            cleaned.startsWith("+63") && cleaned.length == 13 -> true
            // 09171234567 (11 digits starting with 0)
            cleaned.startsWith("0") && cleaned.length == 11 -> true
            // 9171234567 (10 digits)
            cleaned.length == 10 -> true
            else -> false
        }
    }
    
    /**
     * Format to standard +63 format
     */
    fun formatPhilippineNumber(number: String): String {
        val cleaned = number.replace(Regex("[^0-9+]"), "")
        
        return when {
            cleaned.startsWith("+63") -> cleaned
            cleaned.startsWith("0") -> "+63${cleaned.substring(1)}"
            cleaned.length == 10 -> "+63$cleaned"
            else -> cleaned
        }
    }
    
    /**
     * Generate Initial Notice message
     */
    fun generateInitialNotice(
        caseNumber: String,
        respondentName: String,
        accusation: String,
        barangayName: String = "Barangay Hall"
    ): String {
        return """
BARANGAY BLOTTER NOTICE

Dear $respondentName,

You are being notified regarding Case #$caseNumber.

Accusation: $accusation

You are requested to appear at the $barangayName within 3 days to give your statement.

Please bring a valid ID.

- $barangayName
        """.trimIndent()
    }
    
    /**
     * Generate Hearing Notice message
     */
    fun generateHearingNotice(
        caseNumber: String,
        respondentName: String,
        hearingDate: String,
        hearingTime: String,
        venue: String = "Barangay Hall"
    ): String {
        return """
HEARING SCHEDULED

Dear $respondentName,

Case #$caseNumber

A hearing has been scheduled:
Date: $hearingDate
Time: $hearingTime
Venue: $venue

Your presence is REQUIRED.

Bring valid ID and any supporting documents.

- Barangay
        """.trimIndent()
    }
    
    /**
     * Generate Reminder message
     */
    fun generateReminder(
        caseNumber: String,
        respondentName: String,
        daysRemaining: Int
    ): String {
        return """
REMINDER

Dear $respondentName,

This is a reminder for Case #$caseNumber.

You have $daysRemaining day(s) remaining to appear at the Barangay Hall.

Please come as soon as possible to avoid further action.

- Barangay
        """.trimIndent()
    }
    
    /**
     * Generate Final Notice message
     */
    fun generateFinalNotice(
        caseNumber: String,
        respondentName: String
    ): String {
        return """
FINAL NOTICE

Dear $respondentName,

This is the FINAL NOTICE for Case #$caseNumber.

You have failed to appear despite previous notices.

Failure to respond will result in the case being elevated to higher authorities.

Contact us immediately.

- Barangay
        """.trimIndent()
    }
    
    /**
     * Send notification with automatic message generation
     */
    fun sendNotification(
        context: Context,
        notification: SmsNotification,
        respondentName: String,
        caseNumber: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ): Boolean {
        if (!hasSmsPermission(context)) {
            onFailure("SMS permission not granted")
            return false
        }
        
        val formattedNumber = formatPhilippineNumber(notification.recipientNumber)
        
        return sendSms(
            phoneNumber = formattedNumber,
            message = notification.messageContent,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
