package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Hearing
import kotlinx.coroutines.flow.Flow

@Dao
interface HearingDao {
    @Query("SELECT * FROM hearings WHERE blotterReportId = :reportId ORDER BY hearingDate DESC")
    fun getHearingsByReportId(reportId: Int): Flow<List<Hearing>>
    
    @Query("SELECT * FROM hearings WHERE id = :hearingId")
    suspend fun getHearingById(hearingId: Int): Hearing?
    
    @Query("SELECT * FROM hearings WHERE status = :status ORDER BY hearingDate ASC")
    fun getHearingsByStatus(status: String): Flow<List<Hearing>>
    
    @Query("SELECT * FROM hearings ORDER BY hearingDate ASC")
    fun getAllHearings(): Flow<List<Hearing>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHearing(hearing: Hearing): Long
    
    @Update
    suspend fun updateHearing(hearing: Hearing)
    
    @Delete
    suspend fun deleteHearing(hearing: Hearing)
    
    @Query("DELETE FROM hearings WHERE blotterReportId = :reportId")
    suspend fun deleteHearingsByReportId(reportId: Int)
}
