package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Respondent
import kotlinx.coroutines.flow.Flow

@Dao
interface RespondentDao {
    @Query("SELECT * FROM respondents WHERE blotterReportId = :reportId")
    fun getRespondentsByReportId(reportId: Int): Flow<List<Respondent>>
    
    @Query("SELECT * FROM respondents WHERE id = :respondentId")
    suspend fun getRespondentById(respondentId: Int): Respondent?
    
    @Query("SELECT * FROM respondents WHERE personId = :personId")
    fun getRespondentsByPersonId(personId: Int): Flow<List<Respondent>>
    
    @Query("SELECT * FROM respondents WHERE cooperationStatus = :status")
    fun getRespondentsByCooperationStatus(status: String): Flow<List<Respondent>>
    
    @Query("SELECT * FROM respondents WHERE cooperationStatus = 'No Response' OR cooperationStatus = 'Notified'")
    fun getRespondentsNeedingAttention(): Flow<List<Respondent>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRespondent(respondent: Respondent): Long
    
    @Update
    suspend fun updateRespondent(respondent: Respondent)
    
    @Delete
    suspend fun deleteRespondent(respondent: Respondent)
    
    @Query("UPDATE respondents SET cooperationStatus = :status WHERE id = :respondentId")
    suspend fun updateCooperationStatus(respondentId: Int, status: String)
    
    @Query("UPDATE respondents SET appearedInPerson = 1, appearanceDate = :date, cooperationStatus = 'Appeared' WHERE id = :respondentId")
    suspend fun markAsAppeared(respondentId: Int, date: Long)
    
    @Query("UPDATE respondents SET statementGiven = 1, statementDate = :date, statement = :statement WHERE id = :respondentId")
    suspend fun recordStatement(respondentId: Int, statement: String, date: Long)
    
    @Query("SELECT COUNT(*) FROM respondents WHERE cooperationStatus = :status")
    suspend fun getCountByCooperationStatus(status: String): Int
}
