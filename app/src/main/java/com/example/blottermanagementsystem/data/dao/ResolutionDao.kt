package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Resolution
import kotlinx.coroutines.flow.Flow

@Dao
interface ResolutionDao {
    @Query("SELECT * FROM resolutions WHERE blotterReportId = :reportId")
    fun getResolutionByReportId(reportId: Int): Flow<Resolution?>
    
    @Query("SELECT * FROM resolutions WHERE id = :resolutionId")
    suspend fun getResolutionById(resolutionId: Int): Resolution?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResolution(resolution: Resolution): Long
    
    @Update
    suspend fun updateResolution(resolution: Resolution)
    
    @Delete
    suspend fun deleteResolution(resolution: Resolution)
}
