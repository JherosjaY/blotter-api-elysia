package com.example.blottermanagementsystem.utils

import android.content.Context
import android.util.Log
import com.example.blottermanagementsystem.BuildConfig
import com.example.blottermanagementsystem.data.repository.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Version Checker - Checks for app updates
 * Compares current version with latest version from backend
 */
object VersionChecker {
    
    private const val TAG = "VersionChecker"
    private const val PREF_NAME = "version_prefs"
    private const val KEY_LAST_CHECK = "last_version_check"
    private const val KEY_DISMISSED_VERSION = "dismissed_version"
    
    data class VersionInfo(
        val currentVersion: Int,
        val currentVersionName: String,
        val latestVersion: Int,
        val latestVersionName: String,
        val minimumVersion: Int,
        val forceUpdate: Boolean,
        val updateMessage: String,
        val updateUrl: String,
        val needsUpdate: Boolean
    )
    
    /**
     * Check if app needs update
     * @return VersionInfo if update available, null otherwise
     */
    suspend fun checkForUpdate(
        context: Context,
        apiRepository: ApiRepository
    ): VersionInfo? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Checking for app updates...")
            
            // Get current version
            val currentVersion = BuildConfig.VERSION_CODE
            val currentVersionName = BuildConfig.VERSION_NAME
            
            Log.d(TAG, "📱 Current version: $currentVersionName ($currentVersion)")
            
            // Check if we should skip (user dismissed recently)
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val dismissedVersion = prefs.getInt(KEY_DISMISSED_VERSION, 0)
            val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0)
            val now = System.currentTimeMillis()
            
            // Check once per day unless force update
            if (now - lastCheck < 24 * 60 * 60 * 1000 && dismissedVersion >= currentVersion) {
                Log.d(TAG, "⏭️ Skipping check (checked recently)")
                return@withContext null
            }
            
            // Get version info from backend
            val result = apiRepository.getAppVersion()
            
            if (result.isSuccess) {
                val versionData = result.getOrNull()
                if (versionData != null) {
                    val latestVersion = versionData.latestVersion
                    val latestVersionName = versionData.latestVersionName
                    val minimumVersion = versionData.minimumVersion
                    val forceUpdate = versionData.forceUpdate || currentVersion < minimumVersion
                    val updateMessage = versionData.updateMessage
                    val updateUrl = versionData.updateUrl
                    
                    Log.d(TAG, "☁️ Latest version: $latestVersionName ($latestVersion)")
                    Log.d(TAG, "⚠️ Minimum version: $minimumVersion")
                    Log.d(TAG, "🔒 Force update: $forceUpdate")
                    
                    // Save last check time
                    prefs.edit().putLong(KEY_LAST_CHECK, now).apply()
                    
                    // Check if update is needed
                    val needsUpdate = currentVersion < latestVersion
                    
                    if (needsUpdate) {
                        Log.d(TAG, "✅ Update available!")
                        return@withContext VersionInfo(
                            currentVersion = currentVersion,
                            currentVersionName = currentVersionName,
                            latestVersion = latestVersion,
                            latestVersionName = latestVersionName,
                            minimumVersion = minimumVersion,
                            forceUpdate = forceUpdate,
                            updateMessage = updateMessage,
                            updateUrl = updateUrl,
                            needsUpdate = true
                        )
                    } else {
                        Log.d(TAG, "✅ App is up to date!")
                        return@withContext null
                    }
                }
            }
            
            Log.d(TAG, "⚠️ Could not check for updates")
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking for updates: ${e.message}", e)
            null
        }
    }
    
    /**
     * Mark version as dismissed (user clicked "Later")
     */
    fun dismissVersion(context: Context, version: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DISMISSED_VERSION, version).apply()
        Log.d(TAG, "⏭️ Version $version dismissed")
    }
    
    /**
     * Clear dismissed version (for testing)
     */
    fun clearDismissed(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_DISMISSED_VERSION).apply()
        Log.d(TAG, "🗑️ Cleared dismissed version")
    }
}
