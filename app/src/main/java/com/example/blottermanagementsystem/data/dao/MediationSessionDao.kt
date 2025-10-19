package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.MediationSession
import kotlinx.coroutines.flow.Flow

@Dao
interface MediationSessionDao {
    @Query("SELECT * FROM mediation_sessions WHERE blotterReportId = :reportId ORDER BY sessionDate DESC")
    fun getMediationSessionsByReportId(reportId: Int): Flow<List<MediationSession>>
    
    @Query("SELECT * FROM mediation_sessions WHERE id = :sessionId")
    suspend fun getMediationSessionById(sessionId: Int): MediationSession?
    
    @Query("SELECT * FROM mediation_sessions WHERE outcome = :outcome ORDER BY sessionDate DESC")
    fun getMediationSessionsByOutcome(outcome: String): Flow<List<MediationSession>>
    
    @Query("SELECT * FROM mediation_sessions WHERE nextSessionScheduled = 1 ORDER BY nextSessionDate ASC")
    fun getUpcomingMediationSessions(): Flow<List<MediationSession>>
    
    @Query("SELECT * FROM mediation_sessions WHERE blotterReportId = :reportId ORDER BY sessionNumber DESC LIMIT 1")
    suspend fun getLatestMediationSession(reportId: Int): MediationSession?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediationSession(session: MediationSession): Long
    
    @Update
    suspend fun updateMediationSession(session: MediationSession)
    
    @Delete
    suspend fun deleteMediationSession(session: MediationSession)
    
    @Query("UPDATE mediation_sessions SET outcome = :outcome, settlementTerms = :terms WHERE id = :sessionId")
    suspend fun updateSessionOutcome(sessionId: Int, outcome: String, terms: String?)
    
    @Query("SELECT COUNT(*) FROM mediation_sessions WHERE blotterReportId = :reportId")
    suspend fun getMediationSessionCount(reportId: Int): Int
    
    @Query("SELECT COUNT(*) FROM mediation_sessions WHERE outcome = 'Settled'")
    suspend fun getSuccessfulMediationCount(): Int
    
    @Query("SELECT COUNT(*) FROM mediation_sessions WHERE outcome = 'Failed'")
    suspend fun getFailedMediationCount(): Int
}
