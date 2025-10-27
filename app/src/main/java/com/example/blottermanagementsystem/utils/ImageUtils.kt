package com.example.blottermanagementsystem.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    
    /**
     * Copy image from content URI to app's internal storage
     * Returns the file path if successful, null otherwise
     */
    fun copyImageToInternalStorage(context: Context, uri: Uri, userId: Int): String? {
        return try {
            // Create profile images directory
            val profileDir = File(context.filesDir, "profile_images")
            if (!profileDir.exists()) {
                profileDir.mkdirs()
            }
            
            // Create unique filename for this user
            val fileName = "profile_$userId.jpg"
            val destFile = File(profileDir, fileName)
            
            // Copy the image
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d("ImageUtils", "✅ Image copied to: ${destFile.absolutePath}")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error copying image: ${e.message}", e)
            null
        }
    }
    
    /**
     * Delete profile image for a user
     */
    fun deleteProfileImage(context: Context, userId: Int): Boolean {
        return try {
            val profileDir = File(context.filesDir, "profile_images")
            val fileName = "profile_$userId.jpg"
            val file = File(profileDir, fileName)
            
            if (file.exists()) {
                file.delete()
                Log.d("ImageUtils", "✅ Profile image deleted")
                true
            } else {
                Log.d("ImageUtils", "⚠️ Profile image not found")
                false
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "❌ Error deleting image: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get profile image file path for a user
     */
    fun getProfileImagePath(context: Context, userId: Int): String? {
        val profileDir = File(context.filesDir, "profile_images")
        val fileName = "profile_$userId.jpg"
        val file = File(profileDir, fileName)
        
        return if (file.exists()) {
            file.absolutePath
        } else {
            null
        }
    }
}
