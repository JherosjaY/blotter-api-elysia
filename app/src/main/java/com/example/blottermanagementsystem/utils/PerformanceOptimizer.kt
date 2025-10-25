package com.example.blottermanagementsystem.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance Optimization Utilities for 60 FPS
 * Implements caching, lazy loading, and state optimization
 */
object PerformanceOptimizer {
    
    // Image cache to prevent reloading
    private val imageCache = ConcurrentHashMap<String, ImageBitmap>()
    
    // Data cache with TTL (Time To Live)
    private val dataCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val ttl: Long = 5 * 60 * 1000 // 5 minutes default
    ) {
        fun isValid(): Boolean = System.currentTimeMillis() - timestamp < ttl
    }
    
    /**
     * Cache data with TTL
     */
    fun <T : Any> cacheData(key: String, data: T, ttlMillis: Long = 5 * 60 * 1000) {
        dataCache[key] = CacheEntry(data as Any, ttl = ttlMillis)
    }
    
    /**
     * Get cached data if valid
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedData(key: String): T? {
        val entry = dataCache[key] as? CacheEntry<T>
        return if (entry?.isValid() == true) entry.data else null
    }
    
    /**
     * Clear expired cache entries
     */
    fun clearExpiredCache() {
        dataCache.entries.removeIf { !it.value.isValid() }
    }
    
    /**
     * Clear all cache
     */
    fun clearAllCache() {
        dataCache.clear()
        imageCache.clear()
    }
}

/**
 * Composable function to remember with cache
 * Prevents unnecessary recompositions
 */
@Composable
fun <T : Any> rememberCached(key: String, calculation: () -> T): T {
    return remember(key) {
        PerformanceOptimizer.getCachedData<T>(key) ?: calculation().also {
            PerformanceOptimizer.cacheData(key, it)
        }
    }
}

/**
 * Stable wrapper for lambda functions
 * Prevents recomposition when lambda doesn't change
 */
@Stable
class StableHolder<T>(val item: T) {
    operator fun invoke(): T = item
}

@Composable
fun <T> rememberStable(calculation: () -> T): StableHolder<T> {
    return remember { StableHolder(calculation()) }
}

/**
 * Debounce state updates to reduce recompositions
 */
@Composable
fun <T> rememberDebouncedState(
    initialValue: T,
    delayMillis: Long = 300
): MutableState<T> {
    val state = remember { mutableStateOf(initialValue) }
    val debouncedValue = remember { mutableStateOf(initialValue) }
    
    LaunchedEffect(state.value) {
        kotlinx.coroutines.delay(delayMillis)
        debouncedValue.value = state.value
    }
    
    return state
}

/**
 * Optimized list state for large datasets
 */
@Composable
fun <T> rememberOptimizedListState(
    items: List<T>,
    pageSize: Int = 20
): State<List<T>> {
    val visibleItems = remember(items) {
        derivedStateOf {
            items.take(pageSize)
        }
    }
    return visibleItems
}

/**
 * Performance monitoring
 */
object PerformanceMonitor {
    private var frameCount = 0
    private var lastFrameTime = System.currentTimeMillis()
    private var fps = 0.0
    
    fun recordFrame() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastFrameTime
        
        if (elapsed >= 1000) {
            fps = (frameCount * 1000.0) / elapsed
            frameCount = 0
            lastFrameTime = currentTime
        }
    }
    
    fun getCurrentFPS(): Double = fps
}
