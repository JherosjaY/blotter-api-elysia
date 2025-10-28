package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.SyncQueue
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncQueue: SyncQueue): Long
    
    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
    fun getPendingSync(): Flow<List<SyncQueue>>
    
    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingSyncList(): List<SyncQueue>
    
    @Query("UPDATE sync_queue SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
    
    @Query("UPDATE sync_queue SET retryCount = :retryCount, lastError = :error WHERE id = :id")
    suspend fun updateRetry(id: Int, retryCount: Int, error: String)
    
    @Query("DELETE FROM sync_queue WHERE synced = 1")
    suspend fun deleteSynced()
    
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Int)
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE synced = 0")
    fun getPendingCount(): Flow<Int>
}
