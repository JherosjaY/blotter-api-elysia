package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.CaseTimeline
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseTimelineDao {
    
    @Query("SELECT * FROM case_timeline WHERE blotterReportId = :reportId ORDER BY timestamp DESC")
    fun getTimelineByReportId(reportId: Int): Flow<List<CaseTimeline>>
    
    @Query("SELECT * FROM case_timeline WHERE blotterReportId = :reportId ORDER BY timestamp DESC")
    suspend fun getTimelineByReportIdSync(reportId: Int): List<CaseTimeline>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(event: CaseTimeline): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvents(events: List<CaseTimeline>)
    
    @Delete
    suspend fun deleteTimelineEvent(event: CaseTimeline)
    
    @Query("DELETE FROM case_timeline WHERE blotterReportId = :reportId")
    suspend fun deleteTimelineByReportId(reportId: Int)
    
    @Query("SELECT COUNT(*) FROM case_timeline WHERE blotterReportId = :reportId")
    suspend fun getTimelineEventCount(reportId: Int): Int
}
