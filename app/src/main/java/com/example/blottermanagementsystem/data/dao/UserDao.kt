package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<User>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<User>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun getUserCountByRole(role: String): Int
    
    @Query("SELECT COUNT(*) FROM users WHERE role NOT IN ('Admin', 'Officer')")
    suspend fun getTotalUserCount(): Int
}
