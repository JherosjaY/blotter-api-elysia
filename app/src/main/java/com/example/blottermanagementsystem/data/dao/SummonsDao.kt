package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Summons
import kotlinx.coroutines.flow.Flow

@Dao
interface SummonsDao {
    @Query("SELECT * FROM summons WHERE blotterReportId = :reportId ORDER BY issueDate DESC")
    fun getSummonsByReportId(reportId: Int): Flow<List<Summons>>
    
    @Query("SELECT * FROM summons WHERE respondentId = :respondentId ORDER BY issueDate DESC")
    fun getSummonsByRespondentId(respondentId: Int): Flow<List<Summons>>
    
    @Query("SELECT * FROM summons WHERE id = :summonsId")
    suspend fun getSummonsById(summonsId: Int): Summons?
    
    @Query("SELECT * FROM summons WHERE deliveryStatus = :status ORDER BY issueDate DESC")
    fun getSummonsByStatus(status: String): Flow<List<Summons>>
    
    @Query("SELECT * FROM summons WHERE deliveryStatus = 'Pending' OR deliveryStatus = 'Served' ORDER BY issueDate DESC")
    fun getPendingSummons(): Flow<List<Summons>>
    
    @Query("SELECT * FROM summons WHERE isComplied = 0 AND deliveryStatus = 'Served' ORDER BY issueDate DESC")
    fun getUncompliedSummons(): Flow<List<Summons>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummons(summons: Summons): Long
    
    @Update
    suspend fun updateSummons(summons: Summons)
    
    @Delete
    suspend fun deleteSummons(summons: Summons)
    
    @Query("UPDATE summons SET deliveryStatus = :status, deliveryNotes = :notes, receivedDate = :date WHERE id = :summonsId")
    suspend fun updateDeliveryStatus(summonsId: Int, status: String, notes: String?, date: Long)
    
    @Query("UPDATE summons SET isComplied = 1, complianceDate = :date, complianceNotes = :notes WHERE id = :summonsId")
    suspend fun markAsComplied(summonsId: Int, date: Long, notes: String?)
    
    @Query("SELECT COUNT(*) FROM summons WHERE blotterReportId = :reportId")
    suspend fun getSummonsCountByReport(reportId: Int): Int
    
    @Query("SELECT COUNT(*) FROM summons WHERE deliveryStatus = 'Pending'")
    suspend fun getPendingSummonsCount(): Int
}
