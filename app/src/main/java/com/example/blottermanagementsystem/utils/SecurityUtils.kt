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
}
