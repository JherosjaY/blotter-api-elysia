package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.CaseTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseTemplateDao {
    
    @Query("SELECT * FROM case_templates WHERE isActive = 1 ORDER BY usageCount DESC")
    fun getAllActiveTemplates(): Flow<List<CaseTemplate>>
    
    @Query("SELECT * FROM case_templates WHERE isActive = 1 ORDER BY usageCount DESC")
    suspend fun getAllActiveTemplatesSync(): List<CaseTemplate>
    
    @Query("SELECT * FROM case_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: Int): CaseTemplate?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CaseTemplate): Long
    
    @Update
    suspend fun updateTemplate(template: CaseTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: CaseTemplate)
    
    @Query("UPDATE case_templates SET usageCount = usageCount + 1 WHERE id = :templateId")
    suspend fun incrementUsageCount(templateId: Int)
}
