package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    @Query("SELECT * FROM status ORDER BY name ASC")
    fun getAllStatuses(): Flow<List<Status>>
    
    @Query("SELECT * FROM status WHERE id = :statusId")
    suspend fun getStatusById(statusId: Int): Status?
    
    @Query("SELECT * FROM status WHERE name = :name")
    suspend fun getStatusByName(name: String): Status?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: Status): Long
    
    @Update
    suspend fun updateStatus(status: Status)
    
    @Delete
    suspend fun deleteStatus(status: Status)
}
