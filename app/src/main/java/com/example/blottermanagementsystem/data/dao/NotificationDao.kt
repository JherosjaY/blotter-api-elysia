package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsByUserId(userId: Int): Flow<List<Notification>>
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotificationsByUserId(userId: Int): Flow<List<Notification>>
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationCount(userId: Int): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long
    
    @Update
    suspend fun updateNotification(notification: Notification)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)
    
    @Delete
    suspend fun deleteNotification(notification: Notification)
    
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: Int)
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllNotificationsByUserId(userId: Int)
}
