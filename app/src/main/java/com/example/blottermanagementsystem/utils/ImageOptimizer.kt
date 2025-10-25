package com.example.blottermanagementsystem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Optimized Image Loading for 60 FPS
 * - Memory caching
 * - Disk caching
 * - Downsampling
 * - Hardware acceleration
 */
object ImageOptimizer {
    
    /**
     * Create optimized ImageLoader with caching
     */
    fun createOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of app memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 2% of disk
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
    
    /**
     * Create optimized image request
     */
    fun createOptimizedRequest(
        context: Context,
        data: Any?,
        targetWidth: Int = 512,
        targetHeight: Int = 512
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(data)
            .size(targetWidth, targetHeight)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .crossfade(150) // Fast crossfade
            .allowHardware(true) // Hardware acceleration
            .build()
    }
    
    /**
     * Downsample large images to prevent OOM
     */
    suspend fun downsampleImage(
        file: File,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(
                options.outWidth,
                options.outHeight,
                maxWidth,
                maxHeight
            )
            
            // Decode with inSampleSize set
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory
            
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Calculate optimal sample size
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Compress image for storage
     */
    suspend fun compressImage(
        bitmap: Bitmap,
        quality: Int = 85
    ): ByteArray = withContext(Dispatchers.IO) {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.toByteArray()
    }
}

/**
 * Composable to get optimized ImageLoader
 */
@Composable
fun rememberOptimizedImageLoader(): ImageLoader {
    val context = LocalContext.current
    return androidx.compose.runtime.remember {
        ImageOptimizer.createOptimizedImageLoader(context)
    }
}
