package com.example.blottermanagementsystem.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    internal val prefs: SharedPreferences = 
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
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LAST_USER_ID = "last_user_id"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_PERMISSIONS_GRANTED = "permissions_granted"
    }
    
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
    
    var userId: Int
        get() = prefs.getInt(KEY_USER_ID, -1)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()
    
    // Last logged-in user ID (persists after logout)
    var lastUserId: Int
        get() = prefs.getInt(KEY_LAST_USER_ID, -1)
        private set(value) = prefs.edit().putInt(KEY_LAST_USER_ID, value).apply()
    
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
        get() {
            val currentUserId = userId
            return if (currentUserId > 0) {
                prefs.getString("profile_emoji_$currentUserId", "👤")
            } else {
                "👤"
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId > 0) {
                prefs.edit().putString("profile_emoji_$currentUserId", value).apply()
            }
        }
    
    var profileImageUri: String?
        get() {
            val currentUserId = userId
            return if (currentUserId > 0) {
                prefs.getString("profile_image_uri_$currentUserId", null)
            } else {
                null
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId > 0) {
                prefs.edit().putString("profile_image_uri_$currentUserId", value).apply()
            }
        }
    
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
    
    // Notification settings
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    var notificationSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATION_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND, value).apply()
    
    // Biometric authentication (per user)
    var biometricEnabled: Boolean
        get() {
            val currentUserId = userId
            return if (currentUserId != -1) {
                prefs.getBoolean("biometric_enabled_user_$currentUserId", false)
            } else {
                prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId != -1) {
                prefs.edit().putBoolean("biometric_enabled_user_$currentUserId", value).apply()
            } else {
                prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
            }
        }
    
    // Biometric PIN (per user)
    var biometricPin: String
        get() {
            val currentUserId = userId
            return if (currentUserId != -1) {
                prefs.getString("biometric_pin_user_$currentUserId", "") ?: ""
            } else {
                ""
            }
        }
        set(value) {
            val currentUserId = userId
            if (currentUserId != -1) {
                prefs.edit().putString("biometric_pin_user_$currentUserId", value).apply()
            }
        }
    
    // Helper to check if biometric is enabled for a specific user
    fun isBiometricEnabledForUser(userId: Int): Boolean {
        return prefs.getBoolean("biometric_enabled_user_$userId", false)
    }
    
    // Helper to get PIN for a specific user
    fun getBiometricPinForUser(userId: Int): String {
        return prefs.getString("biometric_pin_user_$userId", "") ?: ""
    }
    
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
            putInt(KEY_LAST_USER_ID, userId)  // Save last user ID
            putString(KEY_USERNAME, username)
            putString(KEY_USER_ROLE, role)
            putString("user_role_$userId", role)  // Save role per user
            putString(KEY_FIRST_NAME, firstName)
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_PROFILE_PHOTO, profilePhoto)
            apply()
        }
    }
    
    // FCM Token
    var fcmToken: String?
        get() = prefs.getString(KEY_FCM_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_FCM_TOKEN, value).apply()
    
    var permissionsGranted: Boolean
        get() = prefs.getBoolean(KEY_PERMISSIONS_GRANTED, false)
        set(value) = prefs.edit().putBoolean(KEY_PERMISSIONS_GRANTED, value).apply()
    
    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_USER_ROLE)
            remove(KEY_FIRST_NAME)
            remove(KEY_LAST_NAME)
            remove(KEY_PROFILE_PHOTO)
            // DON'T remove per-user profile data (profile_image_uri_*, profile_emoji_*, etc.)
            // DON'T remove FCM token - it's device-specific, not user-specific
            // This allows users to keep their profile pictures when they log back in
            apply()
        }
    }
}
