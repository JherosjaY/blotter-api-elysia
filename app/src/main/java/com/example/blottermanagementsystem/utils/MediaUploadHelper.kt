package com.example.blottermanagementsystem.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Helper class for uploading photos and videos to Firebase Storage
 * Enables cloud-synced media across multiple devices
 */
object MediaUploadHelper {
    
    private const val TAG = "MediaUploadHelper"
    private const val PHOTOS_FOLDER = "report_photos"
    private const val VIDEOS_FOLDER = "report_videos"
    
    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    
    private val storageRef: StorageReference by lazy {
        storage.reference
    }
    
    /**
     * Upload photo to Firebase Storage
     * @param context Application context
     * @param photoUri Local URI of the photo
     * @param reportId Report ID for organizing files
     * @return Cloud URL of the uploaded photo, or null if failed
     */
    suspend fun uploadPhotoToCloud(
        context: Context,
        photoUri: Uri,
        reportId: Int
    ): String? {
        return try {
            Log.d(TAG, "📸 Uploading photo to Firebase Storage...")
            
            // Generate unique filename
            val fileName = "report_${reportId}_photo_${UUID.randomUUID()}.jpg"
            val photoRef = storageRef.child("$PHOTOS_FOLDER/$fileName")
            
            Log.d(TAG, "📤 Uploading to: $PHOTOS_FOLDER/$fileName")
            
            // Upload file
            val uploadTask = photoRef.putFile(photoUri)
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = photoRef.downloadUrl.await()
            val cloudUrl = downloadUrl.toString()
            
            Log.d(TAG, "✅ Photo uploaded successfully!")
            Log.d(TAG, "🔗 Cloud URL: $cloudUrl")
            
            cloudUrl
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading photo: ${e.message}", e)
            null
        }
    }
    
    /**
     * Upload video to Firebase Storage
     * @param context Application context
     * @param videoUri Local URI of the video
     * @param reportId Report ID for organizing files
     * @return Cloud URL of the uploaded video, or null if failed
     */
    suspend fun uploadVideoToCloud(
        context: Context,
        videoUri: Uri,
        reportId: Int
    ): String? {
        return try {
            Log.d(TAG, "🎥 Uploading video to Firebase Storage...")
            
            // Generate unique filename
            val fileName = "report_${reportId}_video_${UUID.randomUUID()}.mp4"
            val videoRef = storageRef.child("$VIDEOS_FOLDER/$fileName")
            
            Log.d(TAG, "📤 Uploading to: $VIDEOS_FOLDER/$fileName")
            
            // Upload file
            val uploadTask = videoRef.putFile(videoUri)
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = videoRef.downloadUrl.await()
            val cloudUrl = downloadUrl.toString()
            
            Log.d(TAG, "✅ Video uploaded successfully!")
            Log.d(TAG, "🔗 Cloud URL: $cloudUrl")
            
            cloudUrl
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading video: ${e.message}", e)
            null
        }
    }
    
    /**
     * Upload multiple photos to Firebase Storage
     * @param context Application context
     * @param photoUris List of local photo URIs
     * @param reportId Report ID for organizing files
     * @param onProgress Callback for upload progress (current, total)
     * @return List of cloud URLs
     */
    suspend fun uploadMultiplePhotos(
        context: Context,
        photoUris: List<Uri>,
        reportId: Int,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<String> {
        val cloudUrls = mutableListOf<String>()
        
        photoUris.forEachIndexed { index, uri ->
            onProgress(index + 1, photoUris.size)
            
            val cloudUrl = uploadPhotoToCloud(context, uri, reportId)
            if (cloudUrl != null) {
                cloudUrls.add(cloudUrl)
            } else {
                // If upload fails, use local URI as fallback
                cloudUrls.add(uri.toString())
            }
        }
        
        return cloudUrls
    }
    
    /**
     * Upload multiple videos to Firebase Storage
     * @param context Application context
     * @param videoUris List of local video URIs
     * @param reportId Report ID for organizing files
     * @param onProgress Callback for upload progress (current, total)
     * @return List of cloud URLs
     */
    suspend fun uploadMultipleVideos(
        context: Context,
        videoUris: List<Uri>,
        reportId: Int,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<String> {
        val cloudUrls = mutableListOf<String>()
        
        videoUris.forEachIndexed { index, uri ->
            onProgress(index + 1, videoUris.size)
            
            val cloudUrl = uploadVideoToCloud(context, uri, reportId)
            if (cloudUrl != null) {
                cloudUrls.add(cloudUrl)
            } else {
                // If upload fails, use local URI as fallback
                cloudUrls.add(uri.toString())
            }
        }
        
        return cloudUrls
    }
    
    /**
     * Delete photo from Firebase Storage
     * @param photoUrl Cloud URL of the photo
     */
    suspend fun deletePhotoFromCloud(photoUrl: String): Boolean {
        return try {
            if (!isCloudUrl(photoUrl)) return false
            
            Log.d(TAG, "🗑️ Deleting photo from Firebase Storage...")
            val photoRef = storage.getReferenceFromUrl(photoUrl)
            photoRef.delete().await()
            
            Log.d(TAG, "✅ Photo deleted from cloud")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting photo: ${e.message}", e)
            false
        }
    }
    
    /**
     * Delete video from Firebase Storage
     * @param videoUrl Cloud URL of the video
     */
    suspend fun deleteVideoFromCloud(videoUrl: String): Boolean {
        return try {
            if (!isCloudUrl(videoUrl)) return false
            
            Log.d(TAG, "🗑️ Deleting video from Firebase Storage...")
            val videoRef = storage.getReferenceFromUrl(videoUrl)
            videoRef.delete().await()
            
            Log.d(TAG, "✅ Video deleted from cloud")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting video: ${e.message}", e)
            false
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
     * Download media from Firebase Storage to local cache
     * @param mediaUrl Cloud URL of the media file
     * @param context Application context
     * @param isVideo True if video, false if photo
     * @return Local file path, or null if failed
     */
    suspend fun downloadMediaFromCloud(
        mediaUrl: String,
        context: Context,
        isVideo: Boolean = false
    ): String? {
        return try {
            if (!isCloudUrl(mediaUrl)) return mediaUrl // Already local
            
            Log.d(TAG, "📥 Downloading media from Firebase Storage...")
            
            // Create local cache file
            val cacheDir = if (isVideo) {
                java.io.File(context.cacheDir, "video_cache")
            } else {
                java.io.File(context.cacheDir, "photo_cache")
            }
            
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val extension = if (isVideo) "mp4" else "jpg"
            val fileName = "cached_${System.currentTimeMillis()}.$extension"
            val localFile = java.io.File(cacheDir, fileName)
            
            // Download file
            val mediaRef = storage.getReferenceFromUrl(mediaUrl)
            mediaRef.getFile(localFile).await()
            
            Log.d(TAG, "✅ Media downloaded to: ${localFile.absolutePath}")
            localFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error downloading media: ${e.message}", e)
            null
        }
    }
}
