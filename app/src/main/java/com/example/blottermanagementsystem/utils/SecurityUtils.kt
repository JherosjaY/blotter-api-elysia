package com.example.blottermanagementsystem.utils

import java.security.MessageDigest

object SecurityUtils {
    
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.fold("") { str, it -> str + "%02x".format(it) }
    }
    
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return hashPassword(password) == hashedPassword
    }
    
    /**
     * Check if a password string is already hashed (SHA-256 = 64 hex characters)
     */
    fun isPasswordHashed(password: String): Boolean {
        return password.length == 64 && password.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }
}
