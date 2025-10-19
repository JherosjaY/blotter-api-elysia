package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.StatusHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusHistoryDao {
    @Query("SELECT * FROM status_history WHERE blotterReportId = :reportId ORDER BY createdAt DESC")
    fun getStatusHistoryByReportId(reportId: Int): Flow<List<StatusHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusHistory(statusHistory: StatusHistory): Long
    
    @Query("DELETE FROM status_history WHERE blotterReportId = :reportId")
    suspend fun deleteStatusHistoryByReportId(reportId: Int)
}
