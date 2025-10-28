package com.example.blottermanagementsystem.utils

import android.content.Context
import android.util.Log
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.SyncQueue
import com.example.blottermanagementsystem.data.repository.ApiRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SyncManager - Handles automatic syncing of offline data to cloud
 * Processes sync queue when internet connection is available
 */
class SyncManager(private val context: Context) {
    
    private val database = BlotterDatabase.getDatabase(context)
    private val syncQueueDao = database.syncQueueDao()
    private val apiRepository = ApiRepository()
    private val gson = Gson()
    
    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_RETRY_COUNT = 3
    }
    
    /**
     * Process all pending items in sync queue
     */
    suspend fun processSyncQueue() {
        try {
            Log.d(TAG, "🔄 Starting sync queue processing...")
            
            val pendingItems = syncQueueDao.getPendingSyncList()
            
            if (pendingItems.isEmpty()) {
                Log.d(TAG, "✅ No pending items to sync")
                return
            }
            
            Log.d(TAG, "📊 Found ${pendingItems.size} items to sync")
            
            pendingItems.forEach { item ->
                processSyncItem(item)
            }
            
            // Clean up synced items
            syncQueueDao.deleteSynced()
            Log.d(TAG, "✅ Sync queue processing complete!")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error processing sync queue: ${e.message}", e)
        }
    }
    
    /**
     * Process a single sync queue item
     */
    private suspend fun processSyncItem(item: SyncQueue) {
        try {
            Log.d(TAG, "📤 Syncing ${item.entityType} (ID: ${item.entityId}, Action: ${item.action})")
            
            val success = when (item.entityType) {
                "User" -> syncUser(item)
                "Report" -> syncReport(item)
                "Respondent" -> syncRespondent(item)
                "Suspect" -> syncSuspect(item)
                "Witness" -> syncWitness(item)
                "Evidence" -> syncEvidence(item)
                "Hearing" -> syncHearing(item)
                "Resolution" -> syncResolution(item)
                "PersonHistory" -> syncPersonHistory(item)
                else -> {
                    Log.w(TAG, "⚠️ Unknown entity type: ${item.entityType}")
                    false
                }
            }
            
            if (success) {
                syncQueueDao.markAsSynced(item.id)
                Log.d(TAG, "✅ ${item.entityType} synced successfully!")
            } else {
                handleSyncFailure(item)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error syncing ${item.entityType}: ${e.message}", e)
            handleSyncFailure(item, e.message)
        }
    }
    
    /**
     * Handle sync failure - update retry count
     */
    private suspend fun handleSyncFailure(item: SyncQueue, error: String? = null) {
        val newRetryCount = item.retryCount + 1
        
        if (newRetryCount >= MAX_RETRY_COUNT) {
            Log.e(TAG, "❌ Max retries reached for ${item.entityType} (ID: ${item.entityId})")
            // Could mark as failed or delete
            syncQueueDao.delete(item.id)
        } else {
            Log.w(TAG, "⚠️ Retry ${newRetryCount}/$MAX_RETRY_COUNT for ${item.entityType}")
            syncQueueDao.updateRetry(item.id, newRetryCount, error ?: "Unknown error")
        }
    }
    
    // Sync methods for each entity type
    private suspend fun syncUser(item: SyncQueue): Boolean {
        return try {
            val user = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.User::class.java)
            val result = apiRepository.register(
                username = user.username,
                password = user.password, // Already hashed
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role
            )
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ User sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncReport(item: SyncQueue): Boolean {
        return try {
            val report = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.BlotterReport::class.java)
            val result = apiRepository.createReport(report)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Report sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncRespondent(item: SyncQueue): Boolean {
        return try {
            val respondent = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.Respondent::class.java)
            val result = apiRepository.createRespondent(respondent)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Respondent sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncSuspect(item: SyncQueue): Boolean {
        return try {
            val suspect = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.Suspect::class.java)
            val result = apiRepository.createSuspect(suspect)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Suspect sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncWitness(item: SyncQueue): Boolean {
        return try {
            val witness = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.Witness::class.java)
            val result = apiRepository.createWitness(witness)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Witness sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncEvidence(item: SyncQueue): Boolean {
        return try {
            val evidence = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.Evidence::class.java)
            val result = apiRepository.createEvidence(evidence)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Evidence sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncHearing(item: SyncQueue): Boolean {
        return try {
            val hearing = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.Hearing::class.java)
            val result = apiRepository.createHearing(hearing)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Hearing sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncResolution(item: SyncQueue): Boolean {
        return try {
            val resolution = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.Resolution::class.java)
            val result = apiRepository.createResolution(resolution)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ Resolution sync error: ${e.message}", e)
            false
        }
    }
    
    private suspend fun syncPersonHistory(item: SyncQueue): Boolean {
        return try {
            val history = gson.fromJson(item.data, com.example.blottermanagementsystem.data.entity.PersonHistory::class.java)
            val result = apiRepository.createPersonHistory(history)
            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "❌ PersonHistory sync error: ${e.message}", e)
            false
        }
    }
}
