package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Witness
import kotlinx.coroutines.flow.Flow

@Dao
interface WitnessDao {
    @Query("SELECT * FROM witnesses WHERE blotterReportId = :reportId")
    fun getWitnessesByReportId(reportId: Int): Flow<List<Witness>>
    
    @Query("SELECT * FROM witnesses WHERE id = :witnessId")
    suspend fun getWitnessById(witnessId: Int): Witness?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWitness(witness: Witness): Long
    
    @Update
    suspend fun updateWitness(witness: Witness)
    
    @Delete
    suspend fun deleteWitness(witness: Witness)
    
    @Query("DELETE FROM witnesses WHERE blotterReportId = :reportId")
    suspend fun deleteWitnessesByReportId(reportId: Int)
}
