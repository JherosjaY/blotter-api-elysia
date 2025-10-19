package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Evidence
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE blotterReportId = :reportId")
    fun getEvidenceByReportId(reportId: Int): Flow<List<Evidence>>
    
    @Query("SELECT * FROM evidence WHERE id = :evidenceId")
    suspend fun getEvidenceById(evidenceId: Int): Evidence?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: Evidence): Long
    
    @Update
    suspend fun updateEvidence(evidence: Evidence)
    
    @Delete
    suspend fun deleteEvidence(evidence: Evidence)
    
    @Query("DELETE FROM evidence WHERE blotterReportId = :reportId")
    suspend fun deleteEvidenceByReportId(reportId: Int)
}
