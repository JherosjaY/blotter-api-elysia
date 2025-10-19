package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentActivityLogs(): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_logs WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getActivityLogsByCaseId(caseId: Int): Flow<List<ActivityLog>>
    
    @Query("SELECT * FROM activity_logs WHERE performedBy = :username ORDER BY timestamp DESC")
    fun getActivityLogsByUser(username: String): Flow<List<ActivityLog>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(activityLog: ActivityLog): Long
    
    @Query("DELETE FROM activity_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)
}
