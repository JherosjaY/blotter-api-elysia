package com.example.blottermanagementsystem.utils

import android.content.Context
import android.util.Log
import com.example.blottermanagementsystem.data.api.ApiConfig
import com.example.blottermanagementsystem.data.repository.ApiRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging Helper
 * Handles FCM token and topic subscriptions
 */
object FCMHelper {
    
    private const val TAG = "FCMHelper"
    
    // Topic names for role-based notifications
    private const val TOPIC_ALL_USERS = "all_users"
    private const val TOPIC_ADMINS = "admins"
    private const val TOPIC_OFFICERS = "officers"
    private const val TOPIC_REGULAR_USERS = "users"
    
    /**
     * Initialize FCM and get token
     */
    fun initializeFCM(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "❌ Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            
            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "✅ FCM Token: $token")
            
            // Save token to preferences
            val preferencesManager = PreferencesManager(context)
            preferencesManager.fcmToken = token
            
            // Subscribe to topics based on user role
            subscribeToTopics(preferencesManager.userRole)
        }
    }
    
    /**
     * Subscribe to topics based on user role
     */
    fun subscribeToTopics(userRole: String?) {
        // Subscribe to "all_users" topic for everyone
        subscribeToTopic(TOPIC_ALL_USERS)
        
        // Subscribe to role-specific topics
        when (userRole) {
            "Admin" -> {
                subscribeToTopic(TOPIC_ADMINS)
                Log.d(TAG, "📢 Subscribed to Admin topics")
            }
            "Officer" -> {
                subscribeToTopic(TOPIC_OFFICERS)
                Log.d(TAG, "📢 Subscribed to Officer topics")
            }
            "User" -> {
                subscribeToTopic(TOPIC_REGULAR_USERS)
                Log.d(TAG, "📢 Subscribed to User topics")
            }
        }
    }
    
    /**
     * Unsubscribe from all topics (on logout)
     */
    fun unsubscribeFromAllTopics() {
        unsubscribeFromTopic(TOPIC_ALL_USERS)
        unsubscribeFromTopic(TOPIC_ADMINS)
        unsubscribeFromTopic(TOPIC_OFFICERS)
        unsubscribeFromTopic(TOPIC_REGULAR_USERS)
        Log.d(TAG, "📢 Unsubscribed from all topics")
    }
    
    /**
     * Subscribe to a specific topic
     */
    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Subscribed to topic: $topic")
                } else {
                    Log.w(TAG, "❌ Failed to subscribe to topic: $topic", task.exception)
                }
            }
    }
    
    /**
     * Unsubscribe from a specific topic
     */
    private fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Unsubscribed from topic: $topic")
                } else {
                    Log.w(TAG, "❌ Failed to unsubscribe from topic: $topic", task.exception)
                }
            }
    }
    
    /**
     * Send FCM token to backend server
     */
    fun sendTokenToServer(context: Context, token: String, userId: Int, userRole: String) {
        Log.d(TAG, "📤 Sending FCM token to backend...")
        Log.d(TAG, "   UserId: $userId")
        Log.d(TAG, "   Role: $userRole")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ApiRepository()
                val result = repository.updateFcmToken(userId, token)
                
                if (result.isSuccess) {
                    Log.d(TAG, "✅ FCM token sent to backend successfully")
                } else {
                    Log.e(TAG, "❌ Failed to send FCM token to backend")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending FCM token: ${e.message}", e)
            }
        }
    }
}
