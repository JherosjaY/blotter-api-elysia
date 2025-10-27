package com.example.blottermanagementsystem.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryUploader {
    
    private const val TAG = "CloudinaryUploader"
    private var isInitialized = false
    
    /**
     * Initialize Cloudinary
     * Call this once in Application class or before first upload
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            val config = mapOf(
                "cloud_name" to "do9ty8tem",
                "api_key" to "331777292844342",
                "api_secret" to "WadMuNA_5INDBmB0gnQyONhUmvg"
            )
            MediaManager.init(context, config)
            isInitialized = true
            Log.d(TAG, "✅ Cloudinary initialized")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Cloudinary: ${e.message}", e)
        }
    }
    
    /**
     * Upload image to Cloudinary
     * Returns the secure URL of the uploaded image
     */
    suspend fun uploadImage(context: Context, imageUri: Uri, userId: Int): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Initialize if not already done
                if (!isInitialized) {
                    initialize(context)
                }
                
                // Create unique public ID for this user
                val publicId = "profile_images/user_$userId"
                
                Log.d(TAG, "📤 Uploading image for user $userId...")
                
                MediaManager.get().upload(imageUri)
                    .option("public_id", publicId)
                    .option("overwrite", true)
                    .option("resource_type", "image")
                    .option("folder", "blotter_profiles")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(TAG, "⏳ Upload started: $requestId")
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d(TAG, "📊 Upload progress: $progress%")
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val secureUrl = resultData["secure_url"] as? String
                            if (secureUrl != null) {
                                Log.d(TAG, "✅ Upload successful: $secureUrl")
                                continuation.resume(Result.success(secureUrl))
                            } else {
                                Log.e(TAG, "❌ No secure URL in response")
                                continuation.resume(Result.failure(Exception("No secure URL in response")))
                            }
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(TAG, "❌ Upload failed: ${error.description}")
                            continuation.resume(Result.failure(Exception(error.description)))
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.w(TAG, "⚠️ Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Upload error: ${e.message}", e)
                continuation.resume(Result.failure(e))
            }
        }
    }
    
    /**
     * Delete image from Cloudinary
     */
    suspend fun deleteImage(userId: Int): Result<Boolean> {
        return try {
            // Note: Deletion requires admin API which needs server-side implementation
            // For now, we'll just overwrite the image when uploading a new one
            Log.d(TAG, "⚠️ Image deletion not implemented (will be overwritten on next upload)")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
