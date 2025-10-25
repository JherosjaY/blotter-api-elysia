package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Officer
import kotlinx.coroutines.flow.Flow

@Dao
interface OfficerDao {
    @Query("SELECT * FROM officers")
    fun getAllOfficers(): Flow<List<Officer>>
    
    @Query("SELECT * FROM officers")
    suspend fun getAllOfficersSync(): List<Officer>
    
    @Query("SELECT * FROM officers WHERE id = :officerId")
    suspend fun getOfficerById(officerId: Int): Officer?
    
    @Query("SELECT * FROM officers WHERE badgeNumber = :badgeNumber LIMIT 1")
    suspend fun getOfficerByBadgeNumber(badgeNumber: String): Officer?
    
    @Query("SELECT * FROM officers WHERE name = :fullName LIMIT 1")
    suspend fun getOfficerByName(fullName: String): Officer?
    
    @Query("SELECT * FROM officers WHERE userId = :userId LIMIT 1")
    suspend fun getOfficerByUserId(userId: Int): Officer?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficer(officer: Officer)
    
    @Update
    suspend fun updateOfficer(officer: Officer)
    
    @Delete
    suspend fun deleteOfficer(officer: Officer)
    
    @Query("SELECT COUNT(*) FROM officers WHERE isAvailable = 1")
    suspend fun getOfficerCount(): Int
}
