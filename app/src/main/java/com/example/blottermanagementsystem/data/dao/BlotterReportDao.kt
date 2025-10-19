package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.BlotterReport
import kotlinx.coroutines.flow.Flow

@Dao
interface BlotterReportDao {
    @Query("SELECT * FROM blotter_reports WHERE isArchived = 0 ORDER BY dateFiled DESC")
    fun getAllActiveReports(): Flow<List<BlotterReport>>
    
    @Query("SELECT * FROM blotter_reports WHERE isArchived = 1 ORDER BY archivedDate DESC")
    fun getAllArchivedReports(): Flow<List<BlotterReport>>
    
    @Query("SELECT * FROM blotter_reports ORDER BY dateFiled DESC")
    suspend fun getAllReportsSync(): List<BlotterReport>
    
    @Query("SELECT * FROM blotter_reports WHERE id = :reportId")
    suspend fun getReportById(reportId: Int): BlotterReport?
    
    @Query("SELECT * FROM blotter_reports WHERE caseNumber = :caseNumber")
    suspend fun getReportByCaseNumber(caseNumber: String): BlotterReport?
    
    @Query("SELECT * FROM blotter_reports WHERE assignedOfficer = :officerName AND isArchived = 0")
    fun getReportsByOfficer(officerName: String): Flow<List<BlotterReport>>
    
    @Query("SELECT * FROM blotter_reports WHERE assignedOfficerIds LIKE '%' || :officerId || '%' AND isArchived = 0")
    fun getReportsByOfficerId(officerId: Int): Flow<List<BlotterReport>>
    
    @Query("SELECT * FROM blotter_reports WHERE status = :status AND isArchived = 0")
    fun getReportsByStatus(status: String): Flow<List<BlotterReport>>
    
    @Query("SELECT * FROM blotter_reports WHERE userId = :userId AND isArchived = 0")
    fun getReportsByUser(userId: Int): Flow<List<BlotterReport>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: BlotterReport): Long
    
    @Update
    suspend fun updateReport(report: BlotterReport)
    
    @Delete
    suspend fun deleteReport(report: BlotterReport)
    
    @Query("SELECT COUNT(*) FROM blotter_reports WHERE isArchived = 0")
    suspend fun getActiveReportCount(): Int
    
    @Query("SELECT COUNT(*) FROM blotter_reports WHERE status = :status AND isArchived = 0")
    suspend fun getReportCountByStatus(status: String): Int
    
    @Query("SELECT COUNT(*) FROM blotter_reports WHERE isArchived = 1")
    suspend fun getArchivedReportCount(): Int
}
