package com.example.blottermanagementsystem.utils

import java.text.SimpleDateFormat
import java.util.*

object ValidationUtils {
    
    /**
     * Validate Philippine phone number format
     * Accepts: 09XXXXXXXXX (11 digits starting with 09)
     */
    fun validatePhoneNumber(phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return cleanNumber.matches(Regex("^09\\d{9}$"))
    }
    
    /**
     * Format phone number to standard format
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        return if (cleanNumber.length == 11) {
            "${cleanNumber.substring(0, 4)}-${cleanNumber.substring(4, 7)}-${cleanNumber.substring(7)}"
        } else {
            phoneNumber
        }
    }
    
    /**
     * Validate email format
     */
    fun validateEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    
    /**
     * Validate date format (YYYY-MM-DD)
     */
    fun validateDateFormat(date: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.isLenient = false
            format.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validate time format (HH:mm)
     */
    fun validateTimeFormat(time: String): Boolean {
        return time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
    }
    
    /**
     * Check if string is not empty and not blank
     */
    fun isNotEmpty(value: String): Boolean {
        return value.isNotBlank()
    }
    
    /**
     * Validate minimum length
     */
    fun validateMinLength(value: String, minLength: Int): Boolean {
        return value.length >= minLength
    }
    
    /**
     * Validate maximum length
     */
    fun validateMaxLength(value: String, maxLength: Int): Boolean {
        return value.length <= maxLength
    }
    
    /**
     * Validate badge number format
     * Accepts: BDG-XXXXXX (any alphanumeric after BDG-)
     */
    fun validateBadgeNumber(badgeNumber: String): Boolean {
        return badgeNumber.matches(Regex("^BDG-[A-Z0-9]{6}$", RegexOption.IGNORE_CASE))
    }
    
    /**
     * Get phone number error message
     */
    fun getPhoneNumberError(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return "Phone number is required"
        if (!validatePhoneNumber(phoneNumber)) {
            return "Invalid format. Use: 09XXXXXXXXX (11 digits)"
        }
        return null
    }
    
    /**
     * Get email error message
     */
    fun getEmailError(email: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!validateEmail(email)) return "Invalid email format"
        return null
    }
    
    /**
     * Get required field error message
     */
    fun getRequiredFieldError(fieldName: String, value: String): String? {
        return if (value.isBlank()) "$fieldName is required" else null
    }
    
    /**
     * Validate case number format
     * Accepts: YYYY-XXXX (Year-Number)
     */
    fun validateCaseNumber(caseNumber: String): Boolean {
        return caseNumber.matches(Regex("^\\d{4}-\\d{4}$"))
    }
    
    /**
     * Generate case number
     */
    fun generateCaseNumber(year: Int, sequence: Int): String {
        return String.format("%04d-%04d", year, sequence)
    }
    
    /**
     * Sanitize input (remove special characters for security)
     */
    fun sanitizeInput(input: String): String {
        return input.replace(Regex("[<>\"']"), "")
    }
    
    /**
     * Validate password strength
     * At least 8 characters, 1 uppercase, 1 lowercase, 1 number
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        val hasMinLength = password.length >= 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        val score = listOf(hasMinLength, hasUpperCase, hasLowerCase, hasDigit, hasSpecialChar).count { it }
        
        return when {
            score < 3 -> PasswordStrength.WEAK
            score < 4 -> PasswordStrength.MEDIUM
            score < 5 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }
    
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }
}
