package com.example.blottermanagementsystem.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "blotter_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_PROFILE_PHOTO = "profile_photo"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_PROFILE_EMOJI = "profile_emoji"
        private const val KEY_HAS_SELECTED_PFP = "has_selected_pfp"
        private const val KEY_LANGUAGE = "user_language"
    }
    
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
    
    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()
    
    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()
    
    var userRole: String?
        get() = prefs.getString(KEY_USER_ROLE, null)
        set(value) = prefs.edit().putString(KEY_USER_ROLE, value).apply()
    
    var firstName: String?
        get() = prefs.getString(KEY_FIRST_NAME, null)
        set(value) = prefs.edit().putString(KEY_FIRST_NAME, value).apply()
    
    var lastName: String?
        get() = prefs.getString(KEY_LAST_NAME, null)
        set(value) = prefs.edit().putString(KEY_LAST_NAME, value).apply()
    
    var profilePhoto: String?
        get() = prefs.getString(KEY_PROFILE_PHOTO, null)
        set(value) = prefs.edit().putString(KEY_PROFILE_PHOTO, value).apply()
    
    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    
    // Per-user dark mode preference
    var darkMode: Boolean
        get() {
            val currentUserId = userId
            return if (currentUserId != -1) {
                prefs.getBoolean("dark_mode_user_$currentUserId", true)
            } else {
                prefs.getBoolean(KEY_DARK_MODE, true)
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId != -1) {
                prefs.edit().putBoolean("dark_mode_user_$currentUserId", value).apply()
            } else {
                prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
            }
        }
    
    var profileEmoji: String?
        get() = prefs.getString(KEY_PROFILE_EMOJI, "👤")
        set(value) = prefs.edit().putString(KEY_PROFILE_EMOJI, value).apply()
    
    var profileImageUri: String?
        get() = prefs.getString("profile_image_uri", null)
        set(value) = prefs.edit().putString("profile_image_uri", value).apply()
    
    var hasSelectedProfilePicture: Boolean
        get() {
            val currentUserId = userId
            return if (currentUserId != -1) {
                prefs.getBoolean("has_selected_pfp_user_$currentUserId", false)
            } else {
                prefs.getBoolean(KEY_HAS_SELECTED_PFP, false)
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId != -1) {
                prefs.edit().putBoolean("has_selected_pfp_user_$currentUserId", value).apply()
            } else {
                prefs.edit().putBoolean(KEY_HAS_SELECTED_PFP, value).apply()
            }
        }
    
    var mustChangePassword: Boolean
        get() {
            val currentUserId = userId
            return if (currentUserId != -1) {
                prefs.getBoolean("must_change_password_user_$currentUserId", false)
            } else {
                prefs.getBoolean("must_change_password", false)
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId != -1) {
                prefs.edit().putBoolean("must_change_password_user_$currentUserId", value).apply()
            } else {
                prefs.edit().putBoolean("must_change_password", value).apply()
            }
        }
    
    // Language preference (per user)
    var userLanguage: String
        get() {
            val currentUserId = userId
            return if (currentUserId != -1) {
                prefs.getString("language_user_$currentUserId", "en") ?: "en"
            } else {
                prefs.getString(KEY_LANGUAGE, "en") ?: "en"
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId != -1) {
                prefs.edit().putString("language_user_$currentUserId", value).apply()
            } else {
                prefs.edit().putString(KEY_LANGUAGE, value).apply()
            }
        }
    
    val role: String
        get() = userRole ?: "User"
    
    fun saveUserSession(
        userId: Int,
        username: String,
        role: String,
        firstName: String,
        lastName: String,
        profilePhoto: String?
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_ROLE, role)
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_PROFILE_PHOTO, profilePhoto)
            apply()
        }
    }
    
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_USER_ROLE)
            remove(KEY_FIRST_NAME)
            remove(KEY_LAST_NAME)
            remove(KEY_PROFILE_PHOTO)
            // DON'T remove hasSelectedProfilePicture - it's per-user now
            apply()
        }
    }
}
