package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Suspect
import kotlinx.coroutines.flow.Flow

@Dao
interface SuspectDao {
    @Query("SELECT * FROM suspects WHERE blotterReportId = :reportId")
    fun getSuspectsByReportId(reportId: Int): Flow<List<Suspect>>
    
    @Query("SELECT * FROM suspects WHERE id = :suspectId")
    suspend fun getSuspectById(suspectId: Int): Suspect?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuspect(suspect: Suspect): Long
    
    @Update
    suspend fun updateSuspect(suspect: Suspect)
    
    @Delete
    suspend fun deleteSuspect(suspect: Suspect)
    
    @Query("DELETE FROM suspects WHERE blotterReportId = :reportId")
    suspend fun deleteSuspectsByReportId(reportId: Int)
}
