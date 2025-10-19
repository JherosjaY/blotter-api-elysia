package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.RespondentStatement
import kotlinx.coroutines.flow.Flow

@Dao
interface RespondentStatementDao {
    @Query("SELECT * FROM respondent_statements WHERE respondentId = :respondentId ORDER BY submittedDate DESC")
    fun getStatementsByRespondentId(respondentId: Int): Flow<List<RespondentStatement>>
    
    @Query("SELECT * FROM respondent_statements WHERE blotterReportId = :reportId ORDER BY submittedDate DESC")
    fun getStatementsByReportId(reportId: Int): Flow<List<RespondentStatement>>
    
    @Query("SELECT * FROM respondent_statements WHERE isVerified = 0 ORDER BY submittedDate DESC")
    fun getUnverifiedStatements(): Flow<List<RespondentStatement>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatement(statement: RespondentStatement): Long
    
    @Update
    suspend fun updateStatement(statement: RespondentStatement)
    
    @Query("UPDATE respondent_statements SET isVerified = 1, verifiedBy = :verifiedBy, verifiedDate = :date WHERE id = :statementId")
    suspend fun verifyStatement(statementId: Int, verifiedBy: String, date: Long)
    
    @Query("UPDATE respondent_statements SET officerNotes = :notes WHERE id = :statementId")
    suspend fun addOfficerNotes(statementId: Int, notes: String)
}
