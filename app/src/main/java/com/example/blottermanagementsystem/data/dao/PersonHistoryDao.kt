package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.PersonHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonHistoryDao {
    @Query("SELECT * FROM person_history WHERE personId = :personId ORDER BY timestamp DESC")
    fun getHistoryByPersonId(personId: Int): Flow<List<PersonHistory>>
    
    @Query("SELECT * FROM person_history WHERE blotterReportId = :reportId ORDER BY timestamp DESC")
    fun getHistoryByReportId(reportId: Int): Flow<List<PersonHistory>>
    
    @Query("SELECT * FROM person_history WHERE activityType = :activityType ORDER BY timestamp DESC")
    fun getHistoryByActivityType(activityType: String): Flow<List<PersonHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PersonHistory): Long
    
    @Query("DELETE FROM person_history WHERE personId = :personId")
    suspend fun deleteHistoryByPersonId(personId: Int)
    
    @Query("SELECT COUNT(*) FROM person_history WHERE personId = :personId")
    suspend fun getHistoryCountByPersonId(personId: Int): Int
    
    @Query("SELECT COUNT(*) FROM person_history WHERE personId = :personId AND activityType = :activityType")
    suspend fun getHistoryCountByPersonIdAndType(personId: Int, activityType: String): Int
}
