package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.KPForm
import kotlinx.coroutines.flow.Flow

@Dao
interface KPFormDao {
    @Query("SELECT * FROM kp_forms WHERE blotterReportId = :reportId ORDER BY issueDate DESC")
    fun getKPFormsByReportId(reportId: Int): Flow<List<KPForm>>
    
    @Query("SELECT * FROM kp_forms WHERE id = :formId")
    suspend fun getKPFormById(formId: Int): KPForm?
    
    @Query("SELECT * FROM kp_forms WHERE formType = :formType ORDER BY issueDate DESC")
    fun getKPFormsByType(formType: String): Flow<List<KPForm>>
    
    @Query("SELECT * FROM kp_forms WHERE status = :status ORDER BY issueDate DESC")
    fun getKPFormsByStatus(status: String): Flow<List<KPForm>>
    
    @Query("SELECT * FROM kp_forms WHERE blotterReportId = :reportId AND formType = :formType ORDER BY issueDate DESC LIMIT 1")
    suspend fun getLatestKPFormByType(reportId: Int, formType: String): KPForm?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKPForm(kpForm: KPForm): Long
    
    @Update
    suspend fun updateKPForm(kpForm: KPForm)
    
    @Delete
    suspend fun deleteKPForm(kpForm: KPForm)
    
    @Query("UPDATE kp_forms SET status = :status, lastModifiedDate = :date WHERE id = :formId")
    suspend fun updateFormStatus(formId: Int, status: String, date: Long)
    
    @Query("UPDATE kp_forms SET documentPath = :path WHERE id = :formId")
    suspend fun updateDocumentPath(formId: Int, path: String)
    
    @Query("SELECT COUNT(*) FROM kp_forms WHERE blotterReportId = :reportId")
    suspend fun getKPFormsCountByReport(reportId: Int): Int
    
    @Query("SELECT COUNT(*) FROM kp_forms WHERE formType = :formType AND status = 'Issued'")
    suspend fun getIssuedFormsCountByType(formType: String): Int
}
