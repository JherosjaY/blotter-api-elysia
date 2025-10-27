package com.example.blottermanagementsystem.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

/**
 * Helper class for uploading audio files to Firebase Storage
 * Enables cloud-synced audio across multiple devices
 */
object AudioUploadHelper {
    
    private const val TAG = "AudioUploadHelper"
    private const val AUDIO_FOLDER = "audio_recordings"
    
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    
    private val storageRef: StorageReference by lazy {
        storage.reference
    }
    
    /**
     * Upload audio file to Firebase Storage
     * @param audioFilePath Local file path of the audio recording
     * @param reportId Report ID for organizing files
     * @return Cloud URL of the uploaded audio file, or null if failed
     */
    suspend fun uploadAudioToCloud(
        audioFilePath: String,
        reportId: Int
    ): String? {
        return try {
            Log.d(TAG, "🎤 Starting audio upload to Firebase Storage...")
            Log.d(TAG, "📂 Local path: $audioFilePath")
            
            // Create file reference
            val audioFile = File(audioFilePath)
            if (!audioFile.exists()) {
                Log.e(TAG, "❌ Audio file not found: $audioFilePath")
                return null
            }
            
            // Generate unique filename
            val fileName = "report_${reportId}_${UUID.randomUUID()}.m4a"
            val audioRef = storageRef.child("$AUDIO_FOLDER/$fileName")
            
            Log.d(TAG, "📤 Uploading to: $AUDIO_FOLDER/$fileName")
            
            // Upload file
            val uploadTask = audioRef.putFile(Uri.fromFile(audioFile))
            
            // Wait for upload to complete
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = audioRef.downloadUrl.await()
            val cloudUrl = downloadUrl.toString()
            
            Log.d(TAG, "✅ Audio uploaded successfully!")
            Log.d(TAG, "🔗 Cloud URL: $cloudUrl")
            
            // Delete local file after successful upload (optional)
            try {
                audioFile.delete()
                Log.d(TAG, "🗑️ Local audio file deleted (saved to cloud)")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Could not delete local file: ${e.message}")
            }
            
            cloudUrl
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading audio: ${e.message}", e)
            null
        }
    }
    
    /**
     * Delete audio file from Firebase Storage
     * @param audioUrl Cloud URL of the audio file
     */
    suspend fun deleteAudioFromCloud(audioUrl: String): Boolean {
        return try {
            Log.d(TAG, "🗑️ Deleting audio from Firebase Storage...")
            
            val audioRef = storage.getReferenceFromUrl(audioUrl)
            audioRef.delete().await()
            
            Log.d(TAG, "✅ Audio deleted from cloud")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting audio: ${e.message}", e)
            false
        }
    }
    
    /**
     * Download audio file from Firebase Storage to local cache
     * @param audioUrl Cloud URL of the audio file
     * @param context Application context
     * @return Local file path, or null if failed
     */
    suspend fun downloadAudioFromCloud(
        audioUrl: String,
        context: Context
    ): String? {
        return try {
            Log.d(TAG, "📥 Downloading audio from Firebase Storage...")
            
            // Create local cache file
            val cacheDir = File(context.cacheDir, "audio_cache")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val fileName = "cached_${System.currentTimeMillis()}.m4a"
            val localFile = File(cacheDir, fileName)
            
            // Download file
            val audioRef = storage.getReferenceFromUrl(audioUrl)
            audioRef.getFile(localFile).await()
            
            Log.d(TAG, "✅ Audio downloaded to: ${localFile.absolutePath}")
            localFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downloading audio: ${e.message}", e)
            null
        }
    }
    
    /**
     * Check if URL is a cloud URL (Firebase Storage)
     */
    fun isCloudUrl(url: String): Boolean {
        return url.startsWith("https://") && 
               (url.contains("firebasestorage.googleapis.com") || 
                url.contains("storage.googleapis.com"))
    }
    
    /**
     * Get file size from Firebase Storage
     */
    suspend fun getAudioFileSize(audioUrl: String): Long {
        return try {
            val audioRef = storage.getReferenceFromUrl(audioUrl)
            val metadata = audioRef.metadata.await()
            metadata.sizeBytes
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting file size: ${e.message}", e)
            0L
        }
    }
    
    /**
     * Format file size for display
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
}
